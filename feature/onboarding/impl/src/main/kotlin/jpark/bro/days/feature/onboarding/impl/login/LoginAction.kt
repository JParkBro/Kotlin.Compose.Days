package jpark.bro.days.feature.onboarding.impl.login

internal sealed interface LoginAction {
    data object GoogleLoginClicked : LoginAction

    /** Root가 Credential Manager로 받은 구글 ID 토큰 */
    data class GoogleIdTokenReceived(
        val idToken: String,
    ) : LoginAction

    /** 계정 선택 실패. [cancelled]면 사용자가 창을 닫은 것이라 알리지 않는다 */
    data class GoogleIdTokenFailed(
        val cancelled: Boolean,
    ) : LoginAction

    data object KakaoLoginClicked : LoginAction

    /** Root가 카카오 SDK로 받은 카카오 액세스 토큰 */
    data class KakaoAccessTokenReceived(
        val accessToken: String,
    ) : LoginAction

    /** 카카오 로그인 실패. [cancelled]면 사용자가 창을 닫은 것이라 알리지 않는다 */
    data class KakaoAccessTokenFailed(
        val cancelled: Boolean,
    ) : LoginAction
}
