package jpark.bro.days.feature.onboarding.impl.login

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.AuthRepository
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.launch

internal class LoginViewModel(
    private val authRepository: AuthRepository,
) : MviViewModel<LoginState, LoginAction, LoginEvent>(LoginState()) {
    override fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.GoogleLoginClicked -> {
                if (currentState.isSigningIn) return
                setState { copy(isSigningIn = true) }
                sendEvent(LoginEvent.RequestGoogleIdToken)
            }

            is LoginAction.GoogleIdTokenReceived -> {
                signIn(action.idToken)
            }

            is LoginAction.GoogleIdTokenFailed -> {
                setState { copy(isSigningIn = false) }
                if (!action.cancelled) sendEvent(LoginEvent.LoginFailed)
            }
        }
    }

    private fun signIn(idToken: String) {
        viewModelScope.launch {
            authRepository
                .signInWithGoogle(idToken)
                .onSuccess { sendEvent(LoginEvent.LoggedIn) }
                .onFailure {
                    setState { copy(isSigningIn = false) }
                    sendEvent(LoginEvent.LoginFailed)
                }
        }
    }
}
