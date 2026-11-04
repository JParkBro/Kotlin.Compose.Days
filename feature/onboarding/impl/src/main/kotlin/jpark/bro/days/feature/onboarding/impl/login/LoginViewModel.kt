package jpark.bro.days.feature.onboarding.impl.login

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.AuthRepository
import jpark.bro.days.core.model.AuthUser
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.launch

internal class LoginViewModel(
    private val authRepository: AuthRepository,
) : MviViewModel<LoginState, LoginAction, LoginEvent>(LoginState()) {
    override fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.GoogleLoginClicked -> {
                startSignIn(LoginEvent.RequestGoogleIdToken)
            }

            is LoginAction.GoogleIdTokenReceived -> {
                signIn { authRepository.signInWithGoogle(action.idToken) }
            }

            is LoginAction.GoogleIdTokenFailed -> {
                tokenFailed(action.cancelled)
            }

            LoginAction.KakaoLoginClicked -> {
                startSignIn(LoginEvent.RequestKakaoAccessToken)
            }

            is LoginAction.KakaoAccessTokenReceived -> {
                signIn { authRepository.signInWithKakao(action.accessToken) }
            }

            is LoginAction.KakaoAccessTokenFailed -> {
                tokenFailed(action.cancelled)
            }
        }
    }

    /** 로그인 중에는 두 버튼 모두 막는다 */
    private fun startSignIn(request: LoginEvent) {
        if (currentState.isSigningIn) return
        setState { copy(isSigningIn = true) }
        sendEvent(request)
    }

    private fun tokenFailed(cancelled: Boolean) {
        setState { copy(isSigningIn = false) }
        if (!cancelled) sendEvent(LoginEvent.LoginFailed)
    }

    private fun signIn(signIn: suspend () -> Result<AuthUser>) {
        viewModelScope.launch {
            signIn()
                .onSuccess { sendEvent(LoginEvent.LoggedIn) }
                .onFailure {
                    setState { copy(isSigningIn = false) }
                    sendEvent(LoginEvent.LoginFailed)
                }
        }
    }
}
