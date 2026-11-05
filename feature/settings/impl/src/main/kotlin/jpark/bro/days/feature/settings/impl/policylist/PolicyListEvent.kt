package jpark.bro.days.feature.settings.impl.policylist

import jpark.bro.days.feature.settings.api.PolicyType

internal sealed interface PolicyListEvent {
    data class OpenPolicy(
        val type: PolicyType,
    ) : PolicyListEvent

    /** oss-licenses 라이브러리의 목록 화면(Activity)은 Root가 연다 */
    data object OpenOssLicenses : PolicyListEvent

    data object NavigateBack : PolicyListEvent
}
