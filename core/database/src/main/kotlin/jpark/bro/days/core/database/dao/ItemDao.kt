package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import jpark.bro.days.core.database.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    /** 삭제되지 않은 항목. 날짜 없는 일정은 뒤로 */
    @Query(
        """
        SELECT * FROM item
        WHERE deleted_at IS NULL
        ORDER BY date IS NULL, date, created_at
        """,
    )
    fun observeItems(): Flow<List<ItemEntity>>

    /** 삭제됐거나 없으면 null */
    @Query("SELECT * FROM item WHERE id = :id AND deleted_at IS NULL")
    fun observeItem(id: String): Flow<ItemEntity?>

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
