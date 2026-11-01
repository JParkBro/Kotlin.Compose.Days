package jpark.bro.days.feature.settings.impl.policylist

import jpark.bro.days.feature.settings.impl.PolicyType

internal sealed interface PolicyListEvent {
    data class OpenPolicy(
        val type: PolicyType,
    ) : PolicyListEvent

    data object NavigateBack : PolicyListEvent
}
