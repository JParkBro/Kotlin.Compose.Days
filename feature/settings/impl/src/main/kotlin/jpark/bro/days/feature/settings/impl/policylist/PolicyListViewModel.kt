package jpark.bro.days.feature.settings.impl.policylist

import jpark.bro.days.core.ui.MviViewModel

internal class PolicyListViewModel :
    MviViewModel<PolicyListState, PolicyListAction, PolicyListEvent>(PolicyListState()) {
    override fun onAction(action: PolicyListAction) {
        when (action) {
            is PolicyListAction.PolicyClicked -> sendEvent(PolicyListEvent.OpenPolicy(action.type))
            PolicyListAction.OssLicensesClicked -> sendEvent(PolicyListEvent.OpenOssLicenses)
            PolicyListAction.FontLicenseClicked -> setState { copy(isFontLicenseOpen = true) }
            PolicyListAction.FontLicenseDismissed -> setState { copy(isFontLicenseOpen = false) }
            PolicyListAction.BackClicked -> sendEvent(PolicyListEvent.NavigateBack)
        }
    }
}
