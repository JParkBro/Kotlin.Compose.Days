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
            AccountAction.LogoutClicked -> logout()
            AccountAction.WithdrawClicked -> sendEvent(AccountEvent.OpenWithdraw)
            AccountAction.BackClicked -> sendEvent(AccountEvent.NavigateBack)
        }
    }

    /** 기기 데이터는 지우지 않는다. 다른 계정으로 로그인하면 그 계정 항목만 보인다 (product-spec 6장) */
    private fun logout() {
        if (currentState.isSigningOut) return
        setState { copy(isSigningOut = true) }
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
