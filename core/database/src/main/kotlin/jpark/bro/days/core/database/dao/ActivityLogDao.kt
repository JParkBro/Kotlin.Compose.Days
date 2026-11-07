package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import jpark.bro.days.core.database.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    /** 최신순. [since] 이전 기록은 보관 기간이 지나 빼고 보여준다 */
    @Query("SELECT * FROM activity_log WHERE user_id = :userId AND created_at >= :since ORDER BY created_at DESC, id")
    fun observeActivities(
        userId: String,
        since: Long,
    ): Flow<List<ActivityLogEntity>>

    /** 기록을 남기고 보관 기간([olderThan] 이전)이 지난 기록을 지운다 */
    @Transaction
    suspend fun insertAndPrune(
        activities: List<ActivityLogEntity>,
        olderThan: Long,
    ) {
        insertActivities(activities)
        deleteOlderThan(olderThan)
    }

    @Insert
    suspend fun insertActivities(activities: List<ActivityLogEntity>)

    @Query("DELETE FROM activity_log WHERE created_at < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long)

    @Query("UPDATE activity_log SET is_read = 1 WHERE user_id = :userId AND id = :id")
    suspend fun markRead(
        userId: String,
        id: String,
    )

    @Query("UPDATE activity_log SET is_read = 1 WHERE user_id = :userId AND is_read = 0")
    suspend fun markAllRead(userId: String)
}
