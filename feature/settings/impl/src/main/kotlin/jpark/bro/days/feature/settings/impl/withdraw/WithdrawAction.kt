package jpark.bro.days.feature.settings.impl.withdraw

import jpark.bro.days.core.model.Reauthentication

internal sealed interface WithdrawAction {
    /** "안내를 모두 확인했어요" */
    data class ConfirmChanged(
        val checked: Boolean,
    ) : WithdrawAction

    /** 탈퇴하기: 본인 확인(다시 로그인)부터 한다 */
    data object WithdrawClicked : WithdrawAction

    data class Reauthenticated(
        val reauthentication: Reauthentication,
    ) : WithdrawAction

    /** 다시 로그인을 닫음([cancelled]) 또는 실패 */
    data class ReauthenticationFailed(
        val cancelled: Boolean,
    ) : WithdrawAction

    data object BackClicked : WithdrawAction
}
