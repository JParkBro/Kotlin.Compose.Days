package jpark.bro.days.feature.settings.impl.policydoc

import jpark.bro.days.feature.settings.api.PolicyType

internal data class PolicyDocState(
    val type: PolicyType,
    val isLoading: Boolean = true,
    val isFailed: Boolean = false,
    /** 바뀌면 웹뷰가 페이지를 다시 불러온다 */
    val reloadCount: Int = 0,
)
