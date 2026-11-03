import { HttpsError } from "firebase-functions/https";

/** 카카오 토큰 정보 (access_token_info 응답에서 쓰는 필드만) */
export interface KakaoTokenInfo {
  /** 카카오 회원번호 */
  id: number;
  /** 토큰을 발급한 카카오 앱 ID */
  appId: number;
}

export interface KakaoAuthDeps {
  /** 우리 카카오 앱 ID (환경 파라미터 KAKAO_APP_ID) */
  expectedAppId: string;
  fetchTokenInfo: (accessToken: string) => Promise<KakaoTokenInfo>;
  createCustomToken: (uid: string) => Promise<string>;
}

/** 카카오가 토큰을 거부함 (만료 · 잘못된 토큰) */
export class KakaoTokenRejectedError extends Error {}

export const MAX_TOKEN_LENGTH = 1024;
const KAKAO_TIMEOUT_MS = 5_000;
const TOKEN_INFO_URL = "https://kapi.kakao.com/v1/user/access_token_info";

/**
 * 카카오 액세스 토큰 → Firebase 커스텀 토큰 (infra-and-billing-protection.md 2-1).
 * 다른 앱에서 받은 토큰을 재사용하지 못하도록 app_id를 확인한다.
 */
export async function issueKakaoCustomToken(
  data: unknown,
  deps: KakaoAuthDeps,
): Promise<{ customToken: string }> {
  const accessToken = (data as { accessToken?: unknown } | null)?.accessToken;
  if (typeof accessToken !== "string" || accessToken.length === 0 || accessToken.length > MAX_TOKEN_LENGTH) {
    throw new HttpsError("invalid-argument", "accessToken이 필요합니다.");
  }

  let info: KakaoTokenInfo;
  try {
    info = await deps.fetchTokenInfo(accessToken);
  } catch (e) {
    if (e instanceof KakaoTokenRejectedError) {
      throw new HttpsError("unauthenticated", "카카오 토큰이 유효하지 않습니다.");
    }
    throw new HttpsError("unavailable", "카카오 서버에 연결하지 못했습니다.");
  }

  if (String(info.appId) !== deps.expectedAppId) {
    throw new HttpsError("unauthenticated", "카카오 토큰이 유효하지 않습니다.");
  }

  const customToken = await deps.createCustomToken(`kakao:${info.id}`);
  return { customToken };
}

/** 카카오 API 호출. 응답은 신뢰하지 않고 필요한 필드의 형식만 확인한다 */
export async function fetchKakaoTokenInfo(
  accessToken: string,
  fetchFn: typeof fetch = fetch,
): Promise<KakaoTokenInfo> {
  const response = await fetchFn(TOKEN_INFO_URL, {
    headers: { Authorization: `Bearer ${accessToken}` },
    signal: AbortSignal.timeout(KAKAO_TIMEOUT_MS),
  });
  if (response.status === 400 || response.status === 401) {
    throw new KakaoTokenRejectedError();
  }
  if (!response.ok) {
    throw new Error(`kakao status ${response.status}`);
  }
  const body = (await response.json()) as { id?: unknown; app_id?: unknown };
  if (!isPositiveSafeInteger(body.id) || !isPositiveSafeInteger(body.app_id)) {
    throw new Error("kakao response malformed");
  }
  return { id: body.id, appId: body.app_id };
}

function isPositiveSafeInteger(value: unknown): value is number {
  return typeof value === "number" && Number.isSafeInteger(value) && value > 0;
}
