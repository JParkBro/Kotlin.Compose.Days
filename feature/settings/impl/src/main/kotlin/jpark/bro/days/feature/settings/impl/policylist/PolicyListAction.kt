package jpark.bro.days.feature.settings.impl.policylist

import jpark.bro.days.feature.settings.api.PolicyType

internal sealed interface PolicyListAction {
    data class PolicyClicked(
        val type: PolicyType,
    ) : PolicyListAction

    data object OssLicensesClicked : PolicyListAction

    /** Pretendard 글꼴은 Gradle 의존성이 아니라 oss-licenses 목록에 없어 따로 보여준다 */
    data object FontLicenseClicked : PolicyListAction

    data object FontLicenseDismissed : PolicyListAction

    data object BackClicked : PolicyListAction
}
