package jpark.bro.days.feature.settings.impl.account

internal sealed interface AccountEvent {
    /** 로그아웃 완료 → app이 온보딩으로 (SessionEndedHandler) */
    data object LoggedOut : AccountEvent

    data object LogoutFailed : AccountEvent

    data object OpenWithdraw : AccountEvent

    data object NavigateBack : AccountEvent
}
