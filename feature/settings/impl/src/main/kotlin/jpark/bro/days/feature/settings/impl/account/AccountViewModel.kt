package jpark.bro.days.feature.settings.impl.account

import jpark.bro.days.core.ui.MviViewModel

internal class AccountViewModel : MviViewModel<AccountState, AccountAction, AccountEvent>(AccountState()) {
    override fun onAction(action: AccountAction) {
        when (action) {
            AccountAction.LogoutClicked -> sendEvent(AccountEvent.LoggedOut)
            AccountAction.WithdrawClicked -> sendEvent(AccountEvent.OpenWithdraw)
            AccountAction.BackClicked -> sendEvent(AccountEvent.NavigateBack)
        }
    }
}
