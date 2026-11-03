import { CloudBillingClient } from "@google-cloud/billing";
import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { logger, setGlobalOptions } from "firebase-functions";
import { onCall } from "firebase-functions/https";
import { defineString } from "firebase-functions/params";
import { onMessagePublished } from "firebase-functions/pubsub";
import { fetchKakaoTokenInfo, issueKakaoCustomToken } from "./kakaoAuth.js";
import { BudgetNotification, stopBillingIfOverBudget } from "./stopBilling.js";

initializeApp();

// Blaze라 호출 · 인스턴스가 곧 비용이다. 리전 고정, 인스턴스 수는 함수마다 작게 (functions/CLAUDE.md)
setGlobalOptions({ region: "asia-northeast3", maxInstances: 1, timeoutSeconds: 10, memory: "256MiB" });

/** 카카오 개발자 콘솔의 앱 ID (dev · prod 네이티브 앱 키가 한 앱에 있어 하나). 비밀 값이 아니다 */
const kakaoAppId = defineString("KAKAO_APP_ID");

/** 카카오 액세스 토큰 → 커스텀 토큰 (uid = kakao:{회원번호}). 로그인 전에 부르므로 request.auth는 없다 */
export const kakaoAuth = onCall({ enforceAppCheck: true, maxInstances: 3 }, (request) =>
  issueKakaoCustomToken(request.data, {
    expectedAppId: kakaoAppId.value(),
    fetchTokenInfo: (accessToken) => fetchKakaoTokenInfo(accessToken),
    createCustomToken: (uid) => getAuth().createCustomToken(uid),
  }),
);

const BILLING_ALERT_TOPIC = "billing-alerts";

/** 예산 알림(Pub/Sub) → 예산 초과 시 결제 계정 연결 해제. 실행 서비스 계정에는 Project Billing Manager만 준다 */
export const stopBilling = onMessagePublished(
  { topic: BILLING_ALERT_TOPIC, maxInstances: 1, timeoutSeconds: 60 },
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
