package jpark.bro.days.feature.onboarding.impl.login

internal sealed interface LoginEvent {
    /** 계정 선택 창은 화면(Activity)이 필요해 Root가 띄운다 */
    data object RequestGoogleIdToken : LoginEvent

    /** 카카오톡 · 카카오 계정 로그인 창도 화면(Activity)이 필요해 Root가 띄운다 */
    data object RequestKakaoAccessToken : LoginEvent

    /** [termsAgreed]면 이미 약관에 동의한 계정이라 온보딩을 끝내고, 아니면 약관 동의로 */
    data class LoggedIn(
        val termsAgreed: Boolean,
    ) : LoginEvent

    data object LoginFailed : LoginEvent
}
