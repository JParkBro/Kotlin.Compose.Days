package jpark.bro.days.feature.settings.impl.withdraw

import jpark.bro.days.core.ui.MviViewModel

internal class WithdrawViewModel : MviViewModel<WithdrawState, WithdrawAction, WithdrawEvent>(WithdrawState()) {
    override fun onAction(action: WithdrawAction) {
        when (action) {
            WithdrawAction.WithdrawConfirmed -> sendEvent(WithdrawEvent.Withdrawn)
            WithdrawAction.BackClicked -> sendEvent(WithdrawEvent.NavigateBack)
        }
    }
}
