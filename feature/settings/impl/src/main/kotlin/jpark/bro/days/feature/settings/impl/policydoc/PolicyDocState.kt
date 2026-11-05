package jpark.bro.days.feature.settings.impl.policydoc

import jpark.bro.days.feature.settings.api.PolicyType

internal data class PolicyDocState(
    val type: PolicyType,
    val isLoading: Boolean = true,
)
