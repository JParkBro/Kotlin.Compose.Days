package jpark.bro.days.core.data.repository

import jpark.bro.days.core.model.AuthUser
import kotlinx.coroutines.flow.Flow

/** 로그인 상태 (product-spec 3장: 비로그인 사용 없음) */
interface AuthRepository {
    fun observeUser(): Flow<AuthUser?>

    /** 앱 시작 직후에도 기기에 저장된 로그인 정보로 바로 알 수 있다 (첫 화면 결정) */
    fun currentUser(): AuthUser?

    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>

    /** 기기 데이터는 지우지 않는다. 화면은 계정별로 걸러 보여준다 (product-spec 6장) */
    suspend fun signOut(): Result<Unit>
}
