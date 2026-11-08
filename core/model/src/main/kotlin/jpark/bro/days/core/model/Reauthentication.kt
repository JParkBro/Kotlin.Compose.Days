package jpark.bro.days.core.model

/** 탈퇴 전 본인 확인으로 다시 로그인해 받은 토큰. 저장 · 로그하지 않는다 */
sealed interface Reauthentication {
    data class Google(
        val idToken: String,
    ) : Reauthentication

    data class Kakao(
        val accessToken: String,
    ) : Reauthentication
}
