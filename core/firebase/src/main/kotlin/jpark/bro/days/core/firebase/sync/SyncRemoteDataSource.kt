package jpark.bro.days.core.firebase.sync

import kotlinx.coroutines.flow.Flow

/**
 * 동기화 원격 저장소 (sync-and-pairing.md 1장). 나중에 자체 서버로 옮기면 구현체만 바꾼다.
 * - observe…: 앱이 포그라운드일 때만 구독한다 (리스너는 읽기 과금). 변경분([sinceMicros] 이후)만 흘려보낸다
 * - get…: 백그라운드 1회성 조회 (WorkManager)
 * - push…: 서버 시각(serverUpdatedAt)을 붙여 batch로 쓴다
 * [sinceMicros]가 null이면 처음부터 받는다.
 */
interface SyncRemoteDataSource {
    /** 내가 멤버인 커플 (연결 · 해제 감지) */
    fun observeCouples(uid: String): Flow<List<CoupleDto>>

    suspend fun getCouples(uid: String): List<CoupleDto>

    suspend fun pushItems(
        coupleId: String,
        items: List<ItemDto>,
    )

    fun observeItems(
        coupleId: String,
        sinceMicros: Long?,
    ): Flow<List<ItemDto>>

    suspend fun getItems(
        coupleId: String,
        sinceMicros: Long?,
    ): List<ItemDto>

    suspend fun pushHiddenAnniversaries(
        coupleId: String,
        hides: List<HiddenAnniversaryDto>,
    )

    fun observeHiddenAnniversaries(
        coupleId: String,
        sinceMicros: Long?,
    ): Flow<List<HiddenAnniversaryDto>>

    suspend fun getHiddenAnniversaries(
        coupleId: String,
        sinceMicros: Long?,
    ): List<HiddenAnniversaryDto>
}
