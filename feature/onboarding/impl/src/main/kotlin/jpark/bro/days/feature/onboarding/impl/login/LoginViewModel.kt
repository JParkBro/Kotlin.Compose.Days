package jpark.bro.days.feature.onboarding.impl.login

import jpark.bro.days.core.ui.MviViewModel

internal class LoginViewModel : MviViewModel<LoginState, LoginAction, LoginEvent>(LoginState()) {
    override fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.KakaoLoginClicked -> sendEvent(LoginEvent.NavigateToTerms)
            LoginAction.GoogleLoginClicked -> sendEvent(LoginEvent.NavigateToTerms)
        }
    }
}
