package jpark.bro.days.core.data.repository

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.account.AccountLocalData
import jpark.bro.days.core.data.auth.ProviderSessions
import jpark.bro.days.core.firebase.auth.FirebaseAuthDataSource
import jpark.bro.days.core.firebase.functions.FirebaseFunctionsDataSource
import jpark.bro.days.core.model.AuthUser
import kotlinx.coroutines.flow.Flow

internal class DefaultAuthRepository(
    private val authDataSource: FirebaseAuthDataSource,
    private val functionsDataSource: FirebaseFunctionsDataSource,
    private val localData: AccountLocalData,
    private val providerSessions: ProviderSessions,
) : AuthRepository {
    override fun observeUser(): Flow<AuthUser?> = authDataSource.observeUser()

    override fun currentUser(): AuthUser? = authDataSource.currentUser

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> =
        suspendRunCatching { authDataSource.signInWithGoogle(idToken) }

    override suspend fun signInWithKakao(accessToken: String): Result<AuthUser> =
        suspendRunCatching {
            authDataSource.signInWithCustomToken(functionsDataSource.kakaoCustomToken(accessToken))
        }

    /** 먼저 로그아웃해 동기화 리스너를 뗀 뒤 이 기기의 이 계정 데이터를 지운다 (product-spec 6장) */
    override suspend fun signOut(): Result<Unit> =
        suspendRunCatching {
            val userId = authDataSource.currentUser?.uid
            authDataSource.signOut()
            if (userId != null) localData.delete(userId)
            providerSessions.clear(unlinkKakao = false)
        }
}
