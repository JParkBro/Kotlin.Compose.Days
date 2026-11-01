package jpark.bro.days.feature.settings.impl.policylist

import jpark.bro.days.core.ui.MviViewModel

internal class PolicyListViewModel :
    MviViewModel<PolicyListState, PolicyListAction, PolicyListEvent>(PolicyListState()) {
    override fun onAction(action: PolicyListAction) {
        when (action) {
            is PolicyListAction.PolicyClicked -> sendEvent(PolicyListEvent.OpenPolicy(action.type))
            PolicyListAction.BackClicked -> sendEvent(PolicyListEvent.NavigateBack)
        }
    }
}
