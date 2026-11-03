package jpark.bro.days.core.data.session

import jpark.bro.days.core.firebase.auth.FirebaseAuthDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 현재 사용자 id(Firebase uid). 작성자 · 계정별 항목 거르기 · 대표 고정 · 알림처럼 사용자별 데이터에 쓴다 */
internal interface UserSession {
    /** 로그아웃 상태면 null */
    fun observeUserId(): Flow<String?>

    /** 로그인하지 않았으면 예외 (Repository가 Result 실패로 바꾼다) */
    suspend fun currentUserId(): String
}

internal class FirebaseUserSession(
    private val authDataSource: FirebaseAuthDataSource,
) : UserSession {
    override fun observeUserId(): Flow<String?> = authDataSource.observeUser().map { it?.uid }

    override suspend fun currentUserId(): String = checkNotNull(authDataSource.currentUser?.uid) { "로그인이 필요합니다." }
}
