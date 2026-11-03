package jpark.bro.days.core.data.repository

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.firebase.auth.FirebaseAuthDataSource
import jpark.bro.days.core.model.AuthUser
import kotlinx.coroutines.flow.Flow

internal class DefaultAuthRepository(
    private val authDataSource: FirebaseAuthDataSource,
) : AuthRepository {
    override fun observeUser(): Flow<AuthUser?> = authDataSource.observeUser()

    override fun currentUser(): AuthUser? = authDataSource.currentUser

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> =
        suspendRunCatching { authDataSource.signInWithGoogle(idToken) }

    override suspend fun signOut(): Result<Unit> = suspendRunCatching { authDataSource.signOut() }
}
