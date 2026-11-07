import { generateKeyPairSync, sign } from "node:crypto";
import { describe, expect, it, vi } from "vitest";
import {
  decideGrant,
  handleRewardCallback,
  IMAGE_BONUS_LIMIT,
  parseVerifierKeys,
  RewardDeps,
} from "../src/admobReward";

const { privateKey, publicKey } = generateKeyPairSync("ec", { namedCurve: "prime256v1" });
const PEM = publicKey.export({ type: "spki", format: "pem" }).toString();
const KEY_ID = "3335741209";
const AD_UNIT = "1234567890";

/** AdMob처럼 signature · key_id를 맨 뒤에 붙여 서명한 쿼리 */
function callback(fields: Record<string, string>, keyId = KEY_ID): string {
  const message = new URLSearchParams({
    ad_network: "5450213213286189855",
    ad_unit: AD_UNIT,
    custom_data: "c1",
    reward_amount: "1",
    reward_item: "photo",
    timestamp: "1762500000000",
    transaction_id: "tx1",
    user_id: "kakao:42",
    ...fields,
  }).toString();
  const signature = sign("sha256", Buffer.from(message), { key: privateKey, dsaEncoding: "der" }).toString("base64url");
  return `${message}&signature=${signature}&key_id=${keyId}`;
}

function deps(overrides: Partial<RewardDeps> = {}): RewardDeps {
  return {
    publicKeys: vi.fn(async () => new Map([[KEY_ID, PEM]])),
    allowedAdUnits: new Set([AD_UNIT]),
    grant: vi.fn(async () => "granted" as const),
    ...overrides,
  };
}

describe("handleRewardCallback", () => {
  it("서명이 맞으면 uid · coupleId · transactionId로 보상함", async () => {
    const d = deps();
    await expect(handleRewardCallback(callback({}), d)).resolves.toEqual({ status: 200, result: "granted" });
    expect(d.grant).toHaveBeenCalledWith("kakao:42", "c1", "tx1");
  });

  it("값을 바꾸면 서명이 틀려 거부함", async () => {
    const d = deps();
    const tampered = callback({}).replace("custom_data=c1", "custom_data=c2");
    await expect(handleRewardCallback(tampered, d)).resolves.toEqual({ status: 403, result: "bad-signature" });
    expect(d.grant).not.toHaveBeenCalled();
  });

  it("모르는 keyId면 키를 한 번 다시 받고, 그래도 없으면 거부함", async () => {
    const publicKeys = vi.fn(async () => new Map<string, string>());
    await expect(handleRewardCallback(callback({}, "999"), deps({ publicKeys }))).resolves.toMatchObject({
      result: "bad-signature",
    });
    expect(publicKeys.mock.calls).toEqual([[false], [true]]);
  });

  it("바뀐 키를 다시 받으면 확인함", async () => {
    const publicKeys = vi.fn(async (refresh: boolean) => (refresh ? new Map([[KEY_ID, PEM]]) : new Map()));
    await expect(handleRewardCallback(callback({}), deps({ publicKeys }))).resolves.toMatchObject({
      result: "granted",
    });
  });

  it.each([
    ["다른 광고 단위", { ad_unit: "999" }],
    ["user_id 없음 (콘솔 URL 확인)", { user_id: "" }],
    ["custom_data 없음", { custom_data: "" }],
    ["경로 조작 coupleId", { custom_data: "c1/usage/images" }],
  ])("%s이면 200으로 무시함", async (_, fields) => {
    const d = deps();
    await expect(handleRewardCallback(callback(fields), d)).resolves.toEqual({ status: 200, result: "ignored" });
    expect(d.grant).not.toHaveBeenCalled();
  });

  it("서명이 없으면 400", async () => {
    await expect(handleRewardCallback("ad_unit=1&user_id=a", deps())).resolves.toEqual({
      status: 400,
      result: "bad-request",
    });
  });
});

describe("decideGrant", () => {
  const couple = { members: ["kakao:42", "kakao:7"], status: "ACTIVE" };

  it("멤버이고 상한 아래면 보상함", () => {
    expect(decideGrant({ uid: "kakao:42", couple, rewardExists: false, bonus: 0 })).toBe("granted");
  });

  it("같은 transactionId는 한 번만", () => {
    expect(decideGrant({ uid: "kakao:42", couple, rewardExists: true, bonus: 0 })).toBe("duplicate");
  });

  it.each([
    ["비멤버", { uid: "kakao:1", couple }],
    ["해제된 커플", { uid: "kakao:42", couple: { ...couple, status: "DISCONNECTED" } }],
    ["없는 커플", { uid: "kakao:42", couple: undefined }],
  ])("%s이면 보상하지 않음", (_, input) => {
    expect(decideGrant({ ...input, rewardExists: false, bonus: 0 })).toBe("not-member");
  });

  it(`bonus가 ${IMAGE_BONUS_LIMIT}이면 더 주지 않음`, () => {
    expect(decideGrant({ uid: "kakao:42", couple, rewardExists: false, bonus: IMAGE_BONUS_LIMIT - 1 })).toBe("granted");
    expect(decideGrant({ uid: "kakao:42", couple, rewardExists: false, bonus: IMAGE_BONUS_LIMIT })).toBe("limit");
  });
});

describe("parseVerifierKeys", () => {
  it("keyId → PEM, 형식이 틀린 항목은 건너뜀", () => {
    const keys = parseVerifierKeys({ keys: [{ keyId: 1, pem: "a" }, { keyId: 2 }, { pem: "c" }] });
    expect([...keys]).toEqual([["1", "a"]]);
    expect(parseVerifierKeys(null).size).toBe(0);
  });
});
