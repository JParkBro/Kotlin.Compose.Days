package jpark.bro.days.core.data.session

import jpark.bro.days.core.common.IdGenerator
import jpark.bro.days.core.datastore.SessionDataStore

/** 현재 사용자 id. 작성자 · 대표 고정 · 알림처럼 사용자별 데이터에 쓴다 */
internal interface UserSession {
    suspend fun currentUserId(): String
}

/**
 * 로그인 전 임시 구현: 처음 쓸 때 UUID를 만들어 기기에 저장한다.
 * 로그인을 만들면 Firebase uid 구현으로 바꾼다 (그 사이 개발 데이터는 출시 전 DB v1 정책대로 지운다).
 */
internal class LocalUserSession(
    private val sessionDataStore: SessionDataStore,
    private val idGenerator: IdGenerator,
) : UserSession {
    override suspend fun currentUserId(): String = sessionDataStore.getOrCreateLocalUserId(idGenerator::newId)
}
