package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import jpark.bro.days.core.database.entity.SyncState
import jpark.bro.days.core.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE id = :id")
    fun observeUser(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM user WHERE id = :id")
    suspend fun getUser(id: String): UserEntity?

    @Upsert
    suspend fun upsertUser(user: UserEntity)

    /** 올려야 할 내 프로필 (PENDING · FAILED). 내 문서만 쓸 수 있으므로 [userId] 행만 */
    @Query("SELECT * FROM user WHERE id = :userId AND sync_state IN ('PENDING', 'FAILED')")
    suspend fun getUnsyncedUser(userId: String): UserEntity?

    @Query("SELECT COUNT(*) FROM user WHERE id = :userId AND sync_state IN ('PENDING', 'FAILED')")
    fun observeUnsyncedCount(userId: String): Flow<Int>

    /** 올린 뒤 상태 변경. 올리는 사이 다시 저장된 행(updated_at이 다름)은 PENDING으로 남긴다 */
    @Query("UPDATE user SET sync_state = :syncState WHERE id = :id AND updated_at = :updatedAt")
    suspend fun setSyncState(
        id: String,
        updatedAt: Long,
        syncState: String,
    )

    /** 받은 프로필 반영. 아직 올리지 않은 내 변경(SYNCED가 아닌 행)은 덮지 않는다 */
    @Transaction
    suspend fun upsertRemoteUser(user: UserEntity) {
        val local = getUser(user.id)
        if (local == null || local.syncState == SyncState.SYNCED.name) {
            upsertUser(user.copy(syncState = SyncState.SYNCED.name))
        }
    }
}
