import { createPublicKey, verify } from "node:crypto";

/**
 * AdMob 보상형 광고 서버 측 확인(SSV) 콜백 → 커플 사진 한도 +1 (infra-and-billing-protection.md 2-3).
 * AdMob 서버가 부르는 공개 HTTPS 함수라 App Check를 강제할 수 없다. 대신 AdMob 서명 · 우리 광고 단위 · 커플 멤버를 확인한다.
 * 앱은 광고를 띄울 때 user_id = uid, custom_data = coupleId를 넣는다.
 */

/** 보상으로 늘릴 수 있는 사진 수 상한 (기본 30 + 30). 남용돼도 커플당 최대 60장 × 300KB */
export const IMAGE_BONUS_LIMIT = 30;

export const ADMOB_VERIFIER_KEYS_URL = "https://www.gstatic.com/admob/reward/verifier-keys.json";

export type GrantResult = "granted" | "duplicate" | "not-member" | "limit";

export type RewardResult = GrantResult | "ignored" | "bad-signature" | "bad-request";

export interface RewardDeps {
  /** 서명 공개 키 (keyId → PEM). [refresh]면 캐시를 무시하고 다시 받는다 */
  publicKeys: (refresh: boolean) => Promise<Map<string, string>>;
  /** 보상을 받을 수 있는 광고 단위 id (`ca-app-pub-…/` 뒤 숫자) */
  allowedAdUnits: ReadonlySet<string>;
  /** 같은 transactionId는 한 번만, 커플 멤버 · ACTIVE · 상한 확인 후 bonus +1 (트랜잭션) */
  grant: (uid: string, coupleId: string, transactionId: string) => Promise<GrantResult>;
}

export interface RewardResponse {
  /** AdMob은 200이 아니면 다시 보낸다. 서명이 맞는 콜백은 처리 여부와 상관없이 200 */
  status: number;
  result: RewardResult;
}

/** [rawQuery]는 `?` 뒤 원문 그대로 (서명이 원문 기준이라 다시 조립하지 않는다) */
export async function handleRewardCallback(rawQuery: string, deps: RewardDeps): Promise<RewardResponse> {
  const signatureIndex = rawQuery.indexOf("&signature=");
  if (signatureIndex < 0) return { status: 400, result: "bad-request" };
  const params = new URLSearchParams(rawQuery);
  const signature = params.get("signature");
  const keyId = params.get("key_id");
  if (!signature || !keyId) return { status: 400, result: "bad-request" };

  const message = Buffer.from(rawQuery.substring(0, signatureIndex), "utf8");
  if (!(await verifySignature(message, signature, keyId, deps))) return { status: 403, result: "bad-signature" };

  const adUnit = params.get("ad_unit") ?? "";
  const uid = params.get("user_id") ?? "";
  const coupleId = params.get("custom_data") ?? "";
  const transactionId = params.get("transaction_id") ?? "";
  // AdMob 콘솔의 "URL 확인" 요청 · 다른 광고 단위 · 앱이 값을 넣지 않은 광고는 보상하지 않는다
  if (!deps.allowedAdUnits.has(adUnit) || !isDocId(uid) || !isDocId(coupleId) || !isDocId(transactionId)) {
    return { status: 200, result: "ignored" };
  }
  return { status: 200, result: await deps.grant(uid, coupleId, transactionId) };
}

async function verifySignature(message: Buffer, signature: string, keyId: string, deps: RewardDeps) {
  let pem = (await deps.publicKeys(false)).get(keyId);
  // 키가 바뀌었을 수 있다: 모르는 keyId일 때만 한 번 다시 받는다
  if (!pem) pem = (await deps.publicKeys(true)).get(keyId);
  if (!pem) return false;
  try {
    return verify("sha256", message, { key: createPublicKey(pem), dsaEncoding: "der" }, Buffer.from(signature, "base64url"));
  } catch {
    return false;
  }
}

/** 트랜잭션 안에서 읽은 값으로 보상 여부를 정한다. "granted"면 bonus +1과 보상 기록을 쓴다 */
export function decideGrant(input: {
  uid: string;
  couple: { members?: unknown; status?: unknown } | undefined;
  rewardExists: boolean;
  bonus: number;
}): GrantResult {
  if (input.rewardExists) return "duplicate";
  const { couple } = input;
  if (!couple || couple.status !== "ACTIVE" || !Array.isArray(couple.members) || !couple.members.includes(input.uid)) {
    return "not-member";
  }
  if (input.bonus >= IMAGE_BONUS_LIMIT) return "limit";
  return "granted";
}

/** Firestore 문서 id로 쓸 수 있는 값만 (경로 조작 방지) */
function isDocId(value: string): boolean {
  return /^[A-Za-z0-9:_-]{1,128}$/.test(value);
}

/** verifier-keys.json → keyId → PEM */
export function parseVerifierKeys(json: unknown): Map<string, string> {
  const keys = new Map<string, string>();
  const list = (json as { keys?: unknown })?.keys;
  if (!Array.isArray(list)) return keys;
  for (const key of list) {
    const { keyId, pem } = key as { keyId?: unknown; pem?: unknown };
    if ((typeof keyId === "number" || typeof keyId === "string") && typeof pem === "string") {
      keys.set(String(keyId), pem);
    }
  }
  return keys;
}
