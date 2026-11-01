package jpark.bro.days.feature.settings.impl.policylist

import jpark.bro.days.feature.settings.impl.PolicyType

internal sealed interface PolicyListAction {
    data class PolicyClicked(
        val type: PolicyType,
    ) : PolicyListAction

    data object BackClicked : PolicyListAction
}
