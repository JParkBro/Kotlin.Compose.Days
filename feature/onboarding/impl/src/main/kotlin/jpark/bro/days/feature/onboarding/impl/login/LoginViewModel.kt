package jpark.bro.days.feature.onboarding.impl.login

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.AuthRepository
import jpark.bro.days.core.data.repository.OnboardingRepository
import jpark.bro.days.core.model.AuthUser
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.launch

internal class LoginViewModel(
    private val authRepository: AuthRepository,
    private val onboardingRepository: OnboardingRepository,
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
                .onSuccess {
                    // 확인하지 못하면(오프라인 등) 약관을 다시 받는다. 다시 동의해도 기록을 덮어쓸 뿐이다
                    val agreed = onboardingRepository.checkTermsAgreed().getOrDefault(false)
                    // 약관에서 뒤로 오면 다시 로그인할 수 있게 푼다
                    setState { copy(isSigningIn = false) }
                    sendEvent(LoginEvent.LoggedIn(termsAgreed = agreed))
                }.onFailure {
                    setState { copy(isSigningIn = false) }
                    sendEvent(LoginEvent.LoginFailed)
                }
        }
    }
}
