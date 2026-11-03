package jpark.bro.days.feature.onboarding.impl.login

internal data class LoginState(
    /** 계정 선택 창 · Firebase 로그인 중에는 버튼을 막는다 */
    val isSigningIn: Boolean = false,
)
