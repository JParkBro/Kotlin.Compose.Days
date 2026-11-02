package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import jpark.bro.days.core.database.entity.UserItemPrefEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserItemPrefDao {
    /** 대표로 고정한 항목 id. 삭제된 항목이면 null */
    @Query(
        """
        SELECT p.item_id FROM user_item_pref p
        JOIN item i ON i.id = p.item_id
        WHERE p.user_id = :userId AND p.is_pinned = 1 AND i.deleted_at IS NULL
        LIMIT 1
        """,
    )
    fun observePinnedItemId(userId: String): Flow<String?>

    /** 대표는 사용자당 1개. 다른 고정을 풀고 [itemId]를 고정한다 */
    @Transaction
    suspend fun pinItem(
        userId: String,
        itemId: String,
        updatedAt: Long,
    ) {
        unpinAll(userId, updatedAt)
        upsertPref(UserItemPrefEntity(userId = userId, itemId = itemId, isPinned = true, updatedAt = updatedAt))
    }

    @Query("UPDATE user_item_pref SET is_pinned = 0, updated_at = :updatedAt WHERE user_id = :userId AND is_pinned = 1")
    suspend fun unpinAll(
        userId: String,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE user_item_pref SET is_pinned = 0, updated_at = :updatedAt
        WHERE user_id = :userId AND item_id = :itemId
        """,
    )
    suspend fun unpinItem(
        userId: String,
        itemId: String,
        updatedAt: Long,
    )

    @Upsert
    suspend fun upsertPref(pref: UserItemPrefEntity)
}
