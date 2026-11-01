package jpark.bro.days.feature.onboarding.impl.login

import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.onboarding.impl.LoginKey
import jpark.bro.days.feature.onboarding.impl.StartModeKey
import jpark.bro.days.feature.onboarding.impl.TermsKey

internal class LoginViewModel : MviViewModel<LoginState, LoginAction, LoginEvent>(LoginState()) {
    override fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.KakaoLoginClicked -> sendEvent(LoginEvent.NavigateToTerms)
            LoginAction.GoogleLoginClicked -> sendEvent(LoginEvent.NavigateToTerms)
        }
    }
}
