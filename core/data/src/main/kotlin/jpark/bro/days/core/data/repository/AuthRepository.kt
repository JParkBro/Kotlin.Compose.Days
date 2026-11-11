package jpark.bro.days.core.data.repository

import jpark.bro.days.core.model.AuthUser
import kotlinx.coroutines.flow.Flow

/** 로그인 상태 (product-spec 3장: 비로그인 사용 없음) */
interface AuthRepository {
    fun observeUser(): Flow<AuthUser?>

    /** 앱 시작 직후에도 기기에 저장된 로그인 정보로 바로 알 수 있다 (첫 화면 결정) */
    fun currentUser(): AuthUser?

    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>

    /** 카카오 액세스 토큰을 kakaoAuth 함수로 커스텀 토큰으로 바꿔 로그인한다 */
    suspend fun signInWithKakao(accessToken: String): Result<AuthUser>

    /** 로그아웃하고 이 기기의 이 계정 데이터를 지운다. 기기 설정(테마 · 알림 시간)은 둔다 (product-spec 6장) */
    suspend fun signOut(): Result<Unit>
}
