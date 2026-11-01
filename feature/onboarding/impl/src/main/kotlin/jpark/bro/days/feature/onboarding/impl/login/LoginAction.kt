package jpark.bro.days.feature.onboarding.impl.login

internal sealed interface LoginAction {
    data object KakaoLoginClicked : LoginAction

    data object GoogleLoginClicked : LoginAction
}
