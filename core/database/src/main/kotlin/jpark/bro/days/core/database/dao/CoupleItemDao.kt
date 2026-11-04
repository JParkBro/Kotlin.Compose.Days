package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import jpark.bro.days.core.database.entity.ItemEntity
import jpark.bro.days.core.database.entity.SyncState

/**
 * 커플 연결 해제 · 재연결 때 공유 항목 처리 (sync-and-pairing.md 5장, product-spec 7장).
 * 해제: 공유 항목을 내 나만 보기 사본으로 복사하고 내 대표 고정 · 알림을 사본으로 옮긴다.
 * 재연결(30일 안): 원본이 다시 보이므로, 해제 뒤 고치지 않은 사본을 지우고 대표 고정 · 알림을 원본으로 되돌린다.
 */
@Dao
interface CoupleItemDao {
    @Query("SELECT * FROM item WHERE couple_id = :coupleId AND visibility = 'SHARED' AND deleted_at IS NULL")
    suspend fun getSharedItems(coupleId: String): List<ItemEntity>

    /** [copiedAt] 이후 고치지 않은 [userId]의 사본 (원본이 [coupleId]의 공유 항목) */
    @Query(
        """
        SELECT copy.* FROM item AS copy
        JOIN item AS original ON copy.copied_from_item_id = original.id
        WHERE original.couple_id = :coupleId AND copy.owner_user_id = :userId
            AND copy.deleted_at IS NULL AND copy.updated_at = :copiedAt
        """,
    )
    suspend fun getUnchangedCopies(
        coupleId: String,
        userId: String,
        copiedAt: Long,
    ): List<ItemEntity>

    @Insert
    suspend fun insertItem(item: ItemEntity)

    /** 나만 보기 사본은 서버로 가지 않으므로 SYNCED */
    @Query("UPDATE item SET deleted_at = :deletedAt, updated_at = :deletedAt, sync_state = 'SYNCED' WHERE id = :id")
    suspend fun softDeleteCopy(
        id: String,
        deletedAt: Long,
    )

    @Query("UPDATE user_item_pref SET item_id = :to WHERE user_id = :userId AND item_id = :from")
    suspend fun movePref(
        userId: String,
        from: String,
        to: String,
    )

    @Query("UPDATE item_reminder SET item_id = :to WHERE user_id = :userId AND item_id = :from")
    suspend fun moveReminders(
        userId: String,
        from: String,
        to: String,
    )

    /**
     * 공유 항목을 [userId]의 나만 보기 사본으로 복사한다. 사본의 updated_at은 [copiedAt](해제 시각)으로 두어
     * 재연결 때 "해제 뒤 고치지 않은 사본"을 알아본다.
     */
    @Transaction
    suspend fun copySharedItems(
        userId: String,
        coupleId: String,
        copiedAt: Long,
        newId: () -> String,
    ) {
        getSharedItems(coupleId).forEach { original ->
            val copyId = newId()
            insertItem(
                original.copy(
                    id = copyId,
                    ownerUserId = userId,
                    coupleId = null,
                    visibility = PRIVATE,
                    updatedBy = userId,
                    updatedAt = copiedAt,
                    serverUpdatedAt = null,
                    syncState = SyncState.SYNCED.name,
                    copiedFromItemId = original.id,
                ),
            )
            movePref(userId, from = original.id, to = copyId)
            moveReminders(userId, from = original.id, to = copyId)
        }
    }

    /** 재연결: 고치지 않은 사본을 지우고 대표 고정 · 알림을 원본으로 되돌린다 */
    @Transaction
    suspend fun removeUnchangedCopies(
        userId: String,
        coupleId: String,
        copiedAt: Long,
        now: Long,
    ) {
        getUnchangedCopies(coupleId, userId, copiedAt).forEach { copy ->
            val originalId = checkNotNull(copy.copiedFromItemId)
            movePref(userId, from = copy.id, to = originalId)
            moveReminders(userId, from = copy.id, to = originalId)
            softDeleteCopy(copy.id, now)
        }
    }
}

private const val PRIVATE = "PRIVATE"
