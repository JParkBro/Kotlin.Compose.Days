package jpark.bro.days.core.firebase.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import jpark.bro.days.core.model.AuthProvider
import jpark.bro.days.core.model.AuthUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

/** Firebase Auth 감싸기. 앱 시작 직후에도 [currentUser]는 기기에 저장된 로그인 정보로 바로 채워진다 */
class FirebaseAuthDataSource(
    private val auth: FirebaseAuth,
) {
    val currentUser: AuthUser? get() = auth.currentUser?.toAuthUser()

    fun observeUser(): Flow<AuthUser?> =
        callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toAuthUser()) }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }.distinctUntilChanged()

    suspend fun signInWithGoogle(idToken: String): AuthUser {
        val result = auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
        return checkNotNull(result.user) { "로그인 결과에 사용자가 없습니다." }.toAuthUser()
    }

    fun signOut() {
        auth.signOut()
    }
}

/** 카카오는 커스텀 토큰이라 uid가 kakao:로 시작한다 (infra-and-billing-protection.md 2-1) */
private fun FirebaseUser.toAuthUser(): AuthUser =
    AuthUser(
        uid = uid,
        provider = if (uid.startsWith(KAKAO_UID_PREFIX)) AuthProvider.KAKAO else AuthProvider.GOOGLE,
        email = email,
    )

private const val KAKAO_UID_PREFIX = "kakao:"
