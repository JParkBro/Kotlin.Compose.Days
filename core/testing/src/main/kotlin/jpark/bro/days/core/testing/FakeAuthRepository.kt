package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.AuthRepository
import jpark.bro.days.core.model.AuthProvider
import jpark.bro.days.core.model.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository(
    initial: AuthUser? = null,
) : AuthRepository {
    val user = MutableStateFlow(initial)

    /** signInWithGoogle로 받은 ID 토큰 */
    val googleIdTokens = mutableListOf<String>()

    /** signInWithKakao로 받은 액세스 토큰 */
    val kakaoAccessTokens = mutableListOf<String>()

    /** true면 다음 로그인 · 로그아웃이 실패한다 */
    var failNext: Boolean = false

    override fun observeUser(): Flow<AuthUser?> = user

    override fun currentUser(): AuthUser? = user.value

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        googleIdTokens += idToken
        if (consumeFailure()) return Result.failure(IllegalStateException("로그인 실패 (테스트)"))
        val signedIn = AuthUser(uid = "google-$idToken", provider = AuthProvider.GOOGLE, email = "hyunwoo@example.com")
        user.value = signedIn
        return Result.success(signedIn)
    }

    override suspend fun signInWithKakao(accessToken: String): Result<AuthUser> {
        kakaoAccessTokens += accessToken
        if (consumeFailure()) return Result.failure(IllegalStateException("로그인 실패 (테스트)"))
        val signedIn = AuthUser(uid = "kakao:4012345678", provider = AuthProvider.KAKAO, email = null)
        user.value = signedIn
        return Result.success(signedIn)
    }

    override suspend fun signOut(): Result<Unit> {
        if (consumeFailure()) return Result.failure(IllegalStateException("로그아웃 실패 (테스트)"))
        user.value = null
        return Result.success(Unit)
    }

    private fun consumeFailure(): Boolean = failNext.also { failNext = false }
}
