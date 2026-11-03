import { HttpsError } from "firebase-functions/https";
import { describe, expect, it, vi } from "vitest";
import {
  fetchKakaoTokenInfo,
  issueKakaoCustomToken,
  KakaoAuthDeps,
  KakaoTokenRejectedError,
  MAX_TOKEN_LENGTH,
} from "../src/kakaoAuth";

const APP_ID = "123456";

function deps(overrides: Partial<KakaoAuthDeps> = {}): KakaoAuthDeps {
  return {
    expectedAppId: APP_ID,
    fetchTokenInfo: vi.fn(async () => ({ id: 4242, appId: Number(APP_ID) })),
    createCustomToken: vi.fn(async (uid: string) => `custom-${uid}`),
    ...overrides,
  };
}

async function expectCode(promise: Promise<unknown>, code: string) {
  const error = await promise.then(
    () => undefined,
    (e: unknown) => e,
  );
  expect(error).toBeInstanceOf(HttpsError);
  expect((error as HttpsError).code).toBe(code);
}

describe("issueKakaoCustomToken", () => {
  it("우리 앱의 토큰이면 kakao:{회원번호}로 커스텀 토큰을 발급함", async () => {
    const d = deps();
    await expect(issueKakaoCustomToken({ accessToken: "token" }, d)).resolves.toEqual({
      customToken: "custom-kakao:4242",
    });
    expect(d.createCustomToken).toHaveBeenCalledWith("kakao:4242");
  });

  it.each([null, {}, { accessToken: 1 }, { accessToken: "" }, { accessToken: "a".repeat(MAX_TOKEN_LENGTH + 1) }])(
    "accessToken 형식이 틀리면 invalid-argument (%j)",
    async (data) => {
      const d = deps();
      await expectCode(issueKakaoCustomToken(data, d), "invalid-argument");
      expect(d.fetchTokenInfo).not.toHaveBeenCalled();
    },
  );

  it("다른 카카오 앱의 토큰이면 발급하지 않음", async () => {
    const d = deps({ fetchTokenInfo: async () => ({ id: 4242, appId: 999 }) });
    await expectCode(issueKakaoCustomToken({ accessToken: "token" }, d), "unauthenticated");
    expect(d.createCustomToken).not.toHaveBeenCalled();
  });

  it("카카오가 토큰을 거부하면 unauthenticated", async () => {
    const d = deps({
      fetchTokenInfo: async () => {
        throw new KakaoTokenRejectedError();
      },
    });
    await expectCode(issueKakaoCustomToken({ accessToken: "token" }, d), "unauthenticated");
  });

  it("카카오 서버 오류면 unavailable", async () => {
    const d = deps({
      fetchTokenInfo: async () => {
        throw new Error("network");
      },
    });
    await expectCode(issueKakaoCustomToken({ accessToken: "token" }, d), "unavailable");
  });
});

describe("fetchKakaoTokenInfo", () => {
  const respond = (status: number, body: unknown) =>
    vi.fn(async () => new Response(JSON.stringify(body), { status })) as unknown as typeof fetch;

  it("토큰을 Bearer로 보내고 회원번호와 앱 ID를 돌려줌", async () => {
    const fetchFn = respond(200, { id: 4242, expires_in: 100, app_id: 123456 });
    await expect(fetchKakaoTokenInfo("token", fetchFn)).resolves.toEqual({ id: 4242, appId: 123456 });
    const init = (fetchFn as unknown as ReturnType<typeof vi.fn>).mock.calls[0][1] as RequestInit;
    expect(init.headers).toEqual({ Authorization: "Bearer token" });
  });

  it.each([400, 401])("%i 응답이면 토큰 거부", async (status) => {
    await expect(fetchKakaoTokenInfo("token", respond(status, {}))).rejects.toBeInstanceOf(KakaoTokenRejectedError);
  });

  it("서버 오류는 거부가 아닌 오류", async () => {
    const error = await fetchKakaoTokenInfo("token", respond(500, {})).catch((e: unknown) => e);
    expect(error).not.toBeInstanceOf(KakaoTokenRejectedError);
  });

  it.each([{ id: "4242", app_id: 1 }, { id: 4242 }, { id: -1, app_id: 1 }, { id: 1.5, app_id: 1 }])(
    "응답 형식이 틀리면 오류 (%j)",
    async (body) => {
      await expect(fetchKakaoTokenInfo("token", respond(200, body))).rejects.toThrow("malformed");
    },
  );
});
