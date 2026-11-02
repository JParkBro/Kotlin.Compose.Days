package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import jpark.bro.days.core.database.entity.ItemReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemReminderDao {
    @Query("SELECT * FROM item_reminder WHERE user_id = :userId AND item_id = :itemId ORDER BY offset_days")
    fun observeReminders(
        userId: String,
        itemId: String,
    ): Flow<List<ItemReminderEntity>>

    /** 항목의 내 알림을 [reminders]로 통째로 바꾼다 */
    @Transaction
    suspend fun replaceReminders(
        userId: String,
        itemId: String,
        reminders: List<ItemReminderEntity>,
    ) {
        deleteReminders(userId, itemId)
        insertReminders(reminders)
    }

    @Query("DELETE FROM item_reminder WHERE user_id = :userId AND item_id = :itemId")
    suspend fun deleteReminders(
        userId: String,
        itemId: String,
    )

    @Insert
    suspend fun insertReminders(reminders: List<ItemReminderEntity>)
}
