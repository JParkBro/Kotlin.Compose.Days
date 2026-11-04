package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import jpark.bro.days.core.database.entity.AnniversaryHideEntity
import jpark.bro.days.core.database.entity.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface AnniversaryHideDao {
    /** 숨겨진(해제되지 않은) 자동 기념일 rule_key */
    @Query("SELECT rule_key FROM anniversary_hide WHERE couple_id = :coupleId AND deleted_at IS NULL")
    fun observeHiddenRuleKeys(coupleId: String): Flow<List<String>>

    @Query("SELECT * FROM anniversary_hide WHERE couple_id = :coupleId AND rule_key = :ruleKey")
    suspend fun getHide(
        coupleId: String,
        ruleKey: String,
    ): AnniversaryHideEntity?

    @Upsert
    suspend fun upsertHide(hide: AnniversaryHideEntity)

    /** [userId]가 올려야 할 것 (PENDING · FAILED): 내 연결된 커플 것만 (규칙이 ACTIVE 커플 멤버만 허용) */
    @Query("SELECT * FROM anniversary_hide WHERE $UNSYNCED_FOR_USER")
    suspend fun getUnsyncedHides(userId: String): List<AnniversaryHideEntity>

    /** [getUnsyncedHides] 개수. 바뀌면 Push 작업을 예약한다 */
    @Query("SELECT COUNT(*) FROM anniversary_hide WHERE $UNSYNCED_FOR_USER")
    fun observeUnsyncedCount(userId: String): Flow<Int>

    /**
     * 올린 뒤 상태 변경. 올리는 사이 다시 바뀐 행(deleted_at이 다름)은 PENDING으로 남긴다.
     * deleted_at이 null일 수 있어 IS로 비교한다.
     */
    @Query(
        """
        UPDATE anniversary_hide SET sync_state = :syncState
        WHERE couple_id = :coupleId AND rule_key = :ruleKey AND deleted_at IS :deletedAt
        """,
    )
    suspend fun setSyncState(
        coupleId: String,
        ruleKey: String,
        deletedAt: Long?,
        syncState: String,
    )

    /** 받은 변경 반영. 아직 올리지 않은 내 변경(SYNCED가 아닌 행)은 덮지 않는다 (sync-and-pairing.md 2장 충돌 처리) */
    @Transaction
    suspend fun upsertRemoteHides(hides: List<AnniversaryHideEntity>) {
        hides.forEach { remote ->
            val local = getHide(remote.coupleId, remote.ruleKey)
            if (local == null || local.syncState == SyncState.SYNCED.name) {
                upsertHide(remote.copy(syncState = SyncState.SYNCED.name))
            }
        }
    }
}

private const val UNSYNCED_FOR_USER = """
    sync_state IN ('PENDING', 'FAILED') AND couple_id IN (
        SELECT id FROM couple
        WHERE status = 'ACTIVE' AND (member1_user_id = :userId OR member2_user_id = :userId)
    )
"""
