package jpark.bro.days.feature.onboarding.impl.login

internal sealed interface LoginEvent {
    /** 계정 선택 창은 화면(Activity)이 필요해 Root가 띄운다 */
    data object RequestGoogleIdToken : LoginEvent

    data object LoggedIn : LoginEvent

    data object LoginFailed : LoginEvent
}
