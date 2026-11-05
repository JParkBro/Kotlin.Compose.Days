package jpark.bro.days.core.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * 자동 기념일 숨김 (product-spec 2-3). 기념일은 계산하고 숨긴 것만 연결된 커플 단위로 저장한다.
 * 저장은 기기에 먼저 하고 동기화가 상대 기기에 전달한다.
 */
interface AnniversaryRepository {
    /** 연결된 커플에서 숨긴 기념일의 ruleKey. 솔로 · 로그아웃이면 빈 집합 */
    fun observeHiddenRuleKeys(): Flow<Set<String>>

    /** 연결된 커플이 없으면 실패 */
    suspend fun setHidden(
        ruleKey: String,
        hidden: Boolean,
    ): Result<Unit>
}
