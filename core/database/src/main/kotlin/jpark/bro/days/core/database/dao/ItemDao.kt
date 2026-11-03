package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import jpark.bro.days.core.database.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    /**
     * [userId] 계정의 삭제되지 않은 항목. 날짜 없는 일정은 뒤로.
     * 로그아웃해도 기기 데이터는 남기고 계정별로 걸러 보여준다 (product-spec 6장). 커플 공유 항목 조건은 동기화(6-5)에서 더한다
     */
    @Query(
        """
        SELECT * FROM item
        WHERE deleted_at IS NULL AND owner_user_id = :userId
        ORDER BY date IS NULL, date, created_at
        """,
    )
    fun observeItems(userId: String): Flow<List<ItemEntity>>

    /** 삭제됐거나 없거나 다른 계정의 항목이면 null */
    @Query("SELECT * FROM item WHERE id = :id AND deleted_at IS NULL AND owner_user_id = :userId")
    fun observeItem(
        id: String,
        userId: String,
    ): Flow<ItemEntity?>

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
