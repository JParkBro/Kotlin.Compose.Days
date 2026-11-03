import { describe, expect, it, vi } from "vitest";
import { BillingDeps, stopBillingIfOverBudget } from "../src/stopBilling";

function deps(billingEnabled = true): BillingDeps {
  return {
    projectId: "days-61328",
    isBillingEnabled: vi.fn(async () => billingEnabled),
    disableBilling: vi.fn(async () => undefined),
  };
}

describe("stopBillingIfOverBudget", () => {
  it("예산을 넘으면 결제 계정 연결을 끊음", async () => {
    const d = deps();
    await expect(stopBillingIfOverBudget({ costAmount: 1001, budgetAmount: 1000 }, d)).resolves.toBe("disabled");
    expect(d.disableBilling).toHaveBeenCalledWith("projects/days-61328");
  });

  it.each([500, 1000])("예산 이하(%i원)면 끊지 않음", async (costAmount) => {
    const d = deps();
    await expect(stopBillingIfOverBudget({ costAmount, budgetAmount: 1000 }, d)).resolves.toBe("under-budget");
    expect(d.isBillingEnabled).not.toHaveBeenCalled();
    expect(d.disableBilling).not.toHaveBeenCalled();
  });

  it("이미 끊겨 있으면 다시 끊지 않음", async () => {
    const d = deps(false);
    await expect(stopBillingIfOverBudget({ costAmount: 2000, budgetAmount: 1000 }, d)).resolves.toBe(
      "already-disabled",
    );
    expect(d.disableBilling).not.toHaveBeenCalled();
  });

  it.each([{}, { costAmount: "2000", budgetAmount: 1000 }, { costAmount: 2000 }])(
    "금액이 없거나 숫자가 아니면 무시함 (%j)",
    async (notification) => {
      const d = deps();
      await expect(stopBillingIfOverBudget(notification, d)).resolves.toBe("ignored");
      expect(d.disableBilling).not.toHaveBeenCalled();
    },
  );
});
