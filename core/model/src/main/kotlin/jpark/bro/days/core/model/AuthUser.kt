package jpark.bro.days.core.model

/** 로그인 수단 (product-spec 3-2: 카카오 · Google) */
enum class AuthProvider {
    GOOGLE,

    /** 커스텀 토큰 (uid = kakao:{회원번호}) */
    KAKAO,
}

/** 로그인한 사용자. [uid]는 Firebase uid로, 항목 작성자 · 사용자별 데이터의 기준이다 */
data class AuthUser(
    val uid: String,
    val provider: AuthProvider,
    val email: String? = null,
)
