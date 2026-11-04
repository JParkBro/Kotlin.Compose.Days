package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import jpark.bro.days.core.database.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    /**
     * [userId] 계정에 보이는 삭제되지 않은 항목. 날짜 없는 일정은 뒤로.
     * 로그아웃해도 기기 데이터는 남기고 계정별로 걸러 보여준다 (product-spec 6장).
     * 보이는 항목 = 내 나만 보기 항목 + 내 연결된(ACTIVE) 커플의 공유 항목(누가 만들었든)
     */
    @Query(
        """
        SELECT * FROM item
        WHERE deleted_at IS NULL AND $VISIBLE_TO_USER
        ORDER BY date IS NULL, date, created_at
        """,
    )
    fun observeItems(userId: String): Flow<List<ItemEntity>>

    /** 삭제됐거나 없거나 이 계정에 보이지 않는 항목이면 null */
    @Query("SELECT * FROM item WHERE id = :id AND deleted_at IS NULL AND $VISIBLE_TO_USER")
    fun observeItem(
        id: String,
        userId: String,
    ): Flow<ItemEntity?>

    /** 이 계정이 고칠 수 있는(= 보이는) 삭제되지 않은 항목. 없으면 null */
    @Query("SELECT * FROM item WHERE id = :id AND deleted_at IS NULL AND $VISIBLE_TO_USER")
    suspend fun getVisibleItem(
        id: String,
        userId: String,
    ): ItemEntity?

    /** [coupleId]가 [userId]의 연결된(ACTIVE) 커플인지. 공유 항목은 이 커플에만 저장한다 */
    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM couple
            WHERE id = :coupleId AND status = 'ACTIVE' AND (member1_user_id = :userId OR member2_user_id = :userId)
        )
        """,
    )
    suspend fun isActiveCoupleOf(
        coupleId: String,
        userId: String,
    ): Boolean

    /** 삭제된 행도 돌려준다 (수정 · 동기화에서 기존 값을 확인할 때) */
    @Query("SELECT * FROM item WHERE id = :id")
    suspend fun getItem(id: String): ItemEntity?

    @Upsert
    suspend fun upsertItem(item: ItemEntity)

    /** soft delete. 공유 항목이면 syncState = PENDING을 넘겨 상대 기기에 삭제를 전달한다 */
    @Query(
        """
        UPDATE item
        SET deleted_at = :deletedAt, updated_at = :deletedAt, updated_by = :updatedBy, sync_state = :syncState
        WHERE id = :id
        """,
    )
    suspend fun softDeleteItem(
        id: String,
        deletedAt: Long,
        updatedBy: String,
        syncState: String,
    )
}

/** 이 계정에 보이는 항목 조건 (`:userId` 필요) */
private const val VISIBLE_TO_USER = """
    (
        (owner_user_id = :userId AND visibility = 'PRIVATE')
        OR (
            visibility = 'SHARED' AND couple_id IN (
                SELECT id FROM couple
                WHERE status = 'ACTIVE' AND (member1_user_id = :userId OR member2_user_id = :userId)
            )
        )
    )
"""
