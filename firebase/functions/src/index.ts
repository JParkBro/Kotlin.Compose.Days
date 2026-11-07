import { CloudBillingClient } from "@google-cloud/billing";
import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { logger, setGlobalOptions } from "firebase-functions";
import { onCall, onRequest } from "firebase-functions/https";
import { defineString } from "firebase-functions/params";
import { onMessagePublished } from "firebase-functions/pubsub";
import {
  ADMOB_VERIFIER_KEYS_URL,
  decideGrant,
  GrantResult,
  handleRewardCallback,
  parseVerifierKeys,
} from "./admobReward.js";
import { fetchKakaoTokenInfo, issueKakaoCustomToken } from "./kakaoAuth.js";
import { BudgetNotification, stopBillingIfOverBudget } from "./stopBilling.js";

initializeApp();

// Blaze라 호출 · 인스턴스가 곧 비용이다. 리전 고정, 인스턴스 수는 함수마다 작게 (functions/CLAUDE.md)
setGlobalOptions({ region: "asia-northeast3", maxInstances: 1, timeoutSeconds: 10, memory: "256MiB" });

/** 카카오 개발자 콘솔의 앱 ID (dev · prod 네이티브 앱 키가 한 앱에 있어 하나). 비밀 값이 아니다 */
const kakaoAppId = defineString("KAKAO_APP_ID");

/**
 * 카카오 액세스 토큰 → 커스텀 토큰 (uid = kakao:{회원번호}). 로그인 전에 부르므로 request.auth는 없다.
 * 커스텀 토큰 서명(signBlob)은 이 함수 전용 서비스 계정이 자기 자신에 대한 토큰 생성자 권한으로 한다 (기본 compute 계정은 Editor라 넓다)
 */
export const kakaoAuth = onCall({ enforceAppCheck: true, maxInstances: 3, serviceAccount: "kakao-auth@" }, (request) =>
  issueKakaoCustomToken(request.data, {
    expectedAppId: kakaoAppId.value(),
    fetchTokenInfo: (accessToken) => fetchKakaoTokenInfo(accessToken),
    createCustomToken: (uid) => getAuth().createCustomToken(uid),
  }),
);

/**
 * 보상형 광고 단위 id (`ca-app-pub-…/` 뒤 숫자, 쉼표로 여럿). 비밀 값이 아니다.
 * 비어 있으면 모든 콜백을 무시한다 (에뮬레이터 · 광고 단위를 만들기 전 배포가 깨지지 않게)
 */
const admobRewardAdUnits = defineString("ADMOB_REWARD_AD_UNITS", { default: "" });

const KEYS_TTL_MILLIS = 24 * 60 * 60 * 1000;
let cachedKeys: { keys: Map<string, string>; fetchedAt: number } | undefined;

async function admobPublicKeys(refresh: boolean): Promise<Map<string, string>> {
  if (!refresh && cachedKeys && Date.now() - cachedKeys.fetchedAt < KEYS_TTL_MILLIS) return cachedKeys.keys;
  const response = await fetch(ADMOB_VERIFIER_KEYS_URL, { signal: AbortSignal.timeout(5000) });
  if (!response.ok) throw new Error(`verifier-keys ${response.status}`);
  cachedKeys = { keys: parseVerifierKeys(await response.json()), fetchedAt: Date.now() };
  return cachedKeys.keys;
}

/**
 * AdMob 보상형 광고 서버 측 확인(SSV) 콜백 → 커플 사진 한도 bonus +1 (infra-and-billing-protection.md 2-3).
 * AdMob 서버가 GET으로 부르므로 공개 함수이고 App Check를 강제할 수 없다 (functions/CLAUDE.md 예외). 서명으로 확인한다.
 * 호출당 Firestore 읽기 3 · 쓰기 2. Firestore 쓰기 권한(datastore.user)만 준 전용 계정 `admob-reward@`으로 실행한다
 */
export const admobReward = onRequest(
  { maxInstances: 1, invoker: "public", serviceAccount: "admob-reward@" },
  async (req, res) => {
    if (req.method !== "GET") {
      res.status(405).end();
      return;
    }
    const queryIndex = req.originalUrl.indexOf("?");
    const rawQuery = queryIndex < 0 ? "" : req.originalUrl.substring(queryIndex + 1);
    const allowedAdUnits = new Set(
      admobRewardAdUnits
        .value()
        .split(",")
        .map((id) => id.trim())
        .filter((id) => id.length > 0),
    );
    try {
      const { status, result } = await handleRewardCallback(rawQuery, {
        publicKeys: admobPublicKeys,
        allowedAdUnits,
        grant: grantImageBonus,
      });
      // uid · coupleId는 로그에 남기지 않는다
      logger.info("admobReward", { result });
      res.status(status).send(result);
    } catch (error) {
      // 키를 받지 못하는 등 일시 오류: 500이면 AdMob이 다시 보낸다
      logger.error("admobReward failed", { message: error instanceof Error ? error.message : "unknown" });
      res.status(500).end();
    }
  },
);

async function grantImageBonus(uid: string, coupleId: string, transactionId: string): Promise<GrantResult> {
  const db = getFirestore();
  const coupleRef = db.doc(`couples/${coupleId}`);
  const usageRef = coupleRef.collection("usage").doc("images");
  const rewardRef = coupleRef.collection("adRewards").doc(transactionId);
  return db.runTransaction(async (tx) => {
    const [couple, usage, reward] = await tx.getAll(coupleRef, usageRef, rewardRef);
    const bonus = usage.get("bonus");
    const result = decideGrant({
      uid,
      couple: couple.data(),
      rewardExists: reward.exists,
      bonus: typeof bonus === "number" ? bonus : 0,
    });
    if (result === "granted") {
      tx.set(usageRef, { bonus: FieldValue.increment(1) }, { merge: true });
      tx.create(rewardRef, { uid, createdAt: FieldValue.serverTimestamp() });
    }
    return result;
  });
}

const BILLING_ALERT_TOPIC = "billing-alerts";

/**
 * 예산 알림(Pub/Sub) → 예산 초과 시 결제 계정 연결 해제.
 * 결제를 끊는 권한(Project Billing Manager)은 이 함수 전용 서비스 계정에만 준다 ("이름@"은 현재 프로젝트로 채워짐)
 */
export const stopBilling = onMessagePublished(
  { topic: BILLING_ALERT_TOPIC, maxInstances: 1, timeoutSeconds: 60, serviceAccount: "stop-billing@" },
  async (event) => {
    const projectId = process.env.GCLOUD_PROJECT;
    if (!projectId) throw new Error("GCLOUD_PROJECT가 없습니다.");
    const billing = new CloudBillingClient();
    const result = await stopBillingIfOverBudget(event.data.message.json as BudgetNotification, {
      projectId,
      isBillingEnabled: async (name) => {
        const [info] = await billing.getProjectBillingInfo({ name });
        return info.billingEnabled === true;
      },
      disableBilling: async (name) => {
        await billing.updateProjectBillingInfo({ name, projectBillingInfo: { billingAccountName: "" } });
      },
    });
    logger.info("stopBilling", { result });
  },
);
