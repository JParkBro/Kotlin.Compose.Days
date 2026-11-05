package jpark.bro.days.feature.settings.impl.policydoc

import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.settings.api.PolicyDocKey

internal class PolicyDocViewModel(
    key: PolicyDocKey,
) : MviViewModel<PolicyDocState, PolicyDocAction, Nothing>(PolicyDocState(type = key.type)) {
    override fun onAction(action: PolicyDocAction) {
        when (action) {
            PolicyDocAction.Reload -> setState { copy(isLoading = true) }
        }
    }
}
