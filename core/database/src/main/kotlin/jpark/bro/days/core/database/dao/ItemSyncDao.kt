package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import jpark.bro.days.core.database.entity.ItemEntity
import jpark.bro.days.core.database.entity.SyncState
import kotlinx.coroutines.flow.Flow

/** 공유 항목 동기화 전용 (core:sync). 화면용 조회 · 저장은 [ItemDao] */
@Dao
interface ItemSyncDao {
    /**
     * [userId]가 올려야 할 공유 항목 (PENDING · FAILED): 내가 마지막으로 고쳤고 내 연결된 커플의 항목만.
     * 규칙이 updatedBy == 나 · ACTIVE 커플만 허용하므로, 같은 기기의 다른 계정 변경은 그 계정이 로그인했을 때 올린다
     */
    @Query("SELECT * FROM item WHERE $UNSYNCED_BY_USER")
    suspend fun getUnsyncedSharedItems(userId: String): List<ItemEntity>

    /** [getUnsyncedSharedItems] 개수. 바뀌면 Push 작업을 예약한다 */
    @Query("SELECT COUNT(*) FROM item WHERE $UNSYNCED_BY_USER")
    fun observeUnsyncedCount(userId: String): Flow<Int>

    /** 올린 뒤 상태 변경. 올리는 사이 다시 저장된 행(updated_at이 다름)은 PENDING으로 남긴다 */
    @Query("UPDATE item SET sync_state = :syncState WHERE id = :id AND updated_at = :updatedAt")
    suspend fun setSyncState(
        id: String,
        updatedAt: Long,
        syncState: String,
    )

    /**
     * 받은 공유 항목 반영 (삭제도 deleted_at으로 온다). 나중에 저장한 쪽이 이긴다(Last-Write-Wins).
     * 단, 아직 올리지 않은 내 변경(SYNCED가 아닌 행)은 덮지 않는다: Push가 끝나면 서버 값이 내 것이 된다 (sync-and-pairing.md 2장)
     */
    @Transaction
    suspend fun upsertRemoteItems(items: List<ItemEntity>) {
        items.forEach { remote ->
            val local = getItem(remote.id)
            if (local == null || local.syncState == SyncState.SYNCED.name) {
                upsertItem(remote.copy(syncState = SyncState.SYNCED.name))
            }
        }
    }

    @Query("SELECT * FROM item WHERE id = :id")
    suspend fun getItem(id: String): ItemEntity?

    @Upsert
    suspend fun upsertItem(item: ItemEntity)
}

/** [userId]가 올려야 할 공유 항목 조건 (`:userId` 필요) */
private const val UNSYNCED_BY_USER = """
    sync_state IN ('PENDING', 'FAILED') AND visibility = 'SHARED' AND updated_by = :userId
    AND couple_id IN (
        SELECT id FROM couple
        WHERE status = 'ACTIVE' AND (member1_user_id = :userId OR member2_user_id = :userId)
    )
"""
