package jpark.bro.days.feature.settings.impl.withdraw

import jpark.bro.days.core.model.AuthProvider

internal sealed interface WithdrawEvent {
    /** 본인 확인: 같은 로그인 수단으로 다시 로그인한다 (Activity가 필요해 Root가 띄운다) */
    data class RequestReauthentication(
        val provider: AuthProvider,
    ) : WithdrawEvent

    data object Withdrawn : WithdrawEvent

    /** 다시 로그인한 계정이 지금 계정과 다름 */
    data object AccountMismatch : WithdrawEvent

    data object WithdrawFailed : WithdrawEvent

    data object NavigateBack : WithdrawEvent
}
