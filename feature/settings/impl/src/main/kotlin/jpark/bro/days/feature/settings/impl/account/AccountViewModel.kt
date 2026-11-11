package jpark.bro.days.feature.settings.impl.account

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.AuthRepository
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class AccountViewModel(
    private val authRepository: AuthRepository,
) : MviViewModel<AccountState, AccountAction, AccountEvent>(AccountState()) {
    init {
        authRepository
            .observeUser()
            .onEach { user -> setState { copy(user = user) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: AccountAction) {
        when (action) {
            AccountAction.LogoutClicked -> setState { copy(isLogoutConfirmVisible = true) }
            AccountAction.LogoutConfirmed -> logout()
            AccountAction.LogoutDismissed -> setState { copy(isLogoutConfirmVisible = false) }
            AccountAction.WithdrawClicked -> sendEvent(AccountEvent.OpenWithdraw)
            AccountAction.BackClicked -> sendEvent(AccountEvent.NavigateBack)
        }
    }

    /** 이 기기의 이 계정 데이터도 지운다 (product-spec 6장). 확인 창에서 먼저 알린다 */
    private fun logout() {
        if (currentState.isSigningOut) return
        setState { copy(isSigningOut = true, isLogoutConfirmVisible = false) }
        viewModelScope.launch {
            authRepository
                .signOut()
                .onSuccess { sendEvent(AccountEvent.LoggedOut) }
                .onFailure {
                    setState { copy(isSigningOut = false) }
                    sendEvent(AccountEvent.LogoutFailed)
                }
        }
    }
}
