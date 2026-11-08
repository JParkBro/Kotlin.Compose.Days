package jpark.bro.days.feature.settings.impl.withdraw

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.account.AccountMismatchException
import jpark.bro.days.core.data.repository.AccountRepository
import jpark.bro.days.core.data.repository.AuthRepository
import jpark.bro.days.core.data.repository.CoupleRepository
import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

/**
 * 회원 탈퇴 (product-spec 6장, sync-and-pairing.md 7장). 안내를 확인하고 같은 로그인 수단으로 다시 로그인하면 탈퇴한다.
 * 커플이면 연결을 해제하고 상대 기기에 공유 항목 사본을 남긴다.
 */
internal class WithdrawViewModel(
    private val accountRepository: AccountRepository,
    authRepository: AuthRepository,
    coupleRepository: CoupleRepository,
    profileRepository: ProfileRepository,
) : MviViewModel<WithdrawState, WithdrawAction, WithdrawEvent>(WithdrawState()) {
    init {
        combine(
            authRepository.observeUser(),
            coupleRepository.observeCouple(),
            profileRepository.observePartnerProfile(),
        ) { user, couple, partner ->
            // 탈퇴가 끝나 로그아웃되면(user == null) 화면을 떠나므로 마지막 값을 둔다
            if (user != null) {
                setState {
                    copy(
                        provider = user.provider,
                        isCoupled = couple != null,
                        partnerName = partner?.nickname?.takeIf { couple != null },
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: WithdrawAction) {
        when (action) {
            is WithdrawAction.ConfirmChanged -> {
                if (!currentState.isWorking) setState { copy(isConfirmed = action.checked) }
            }

            WithdrawAction.WithdrawClicked -> {
                val provider = currentState.provider
                if (provider == null || !currentState.canWithdraw) return
                setState { copy(isWorking = true) }
                sendEvent(WithdrawEvent.RequestReauthentication(provider))
            }

            is WithdrawAction.Reauthenticated -> {
                withdraw(action)
            }

            is WithdrawAction.ReauthenticationFailed -> {
                setState { copy(isWorking = false) }
                if (!action.cancelled) sendEvent(WithdrawEvent.WithdrawFailed)
            }

            WithdrawAction.BackClicked -> {
                if (!currentState.isWorking) sendEvent(WithdrawEvent.NavigateBack)
            }
        }
    }

    private fun withdraw(action: WithdrawAction.Reauthenticated) {
        viewModelScope.launch {
            accountRepository
                .withdraw(action.reauthentication)
                .onSuccess { sendEvent(WithdrawEvent.Withdrawn) }
                .onFailure { error ->
                    setState { copy(isWorking = false) }
                    val mismatch = error is AccountMismatchException
                    sendEvent(if (mismatch) WithdrawEvent.AccountMismatch else WithdrawEvent.WithdrawFailed)
                }
        }
    }
}
