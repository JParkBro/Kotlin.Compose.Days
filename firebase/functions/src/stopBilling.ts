/** Cloud Billing 예산 알림 메시지 (쓰는 필드만) */
export interface BudgetNotification {
  costAmount?: unknown;
  budgetAmount?: unknown;
}

export interface BillingDeps {
  projectId: string;
  isBillingEnabled: (projectName: string) => Promise<boolean>;
  disableBilling: (projectName: string) => Promise<void>;
}

export type StopBillingResult = "under-budget" | "already-disabled" | "disabled" | "ignored";

/**
 * 예산을 넘으면 프로젝트의 결제 계정 연결을 끊는다 (infra-and-billing-protection.md 4-2).
 * 끊기면 Firestore · Storage · Functions가 모두 멈추고 앱은 Room 데이터로 계속 동작한다.
 */
export async function stopBillingIfOverBudget(
  notification: BudgetNotification,
  deps: BillingDeps,
): Promise<StopBillingResult> {
  const { costAmount, budgetAmount } = notification;
  if (typeof costAmount !== "number" || typeof budgetAmount !== "number") {
    return "ignored";
  }
  if (costAmount <= budgetAmount) {
    return "under-budget";
  }

  const projectName = `projects/${deps.projectId}`;
  if (!(await deps.isBillingEnabled(projectName))) {
    return "already-disabled";
  }
  await deps.disableBilling(projectName);
  return "disabled";
}
