package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import jpark.bro.days.core.database.entity.ItemEntity
import jpark.bro.days.core.database.entity.ItemReminderEntity
import jpark.bro.days.core.database.entity.UserItemPrefEntity

/**
 * 백업 · 복원 (product-spec 6장): [userId] 계정의 나만 보기 항목과 사용자별 설정(대표 고정 · 알림).
 * 우리 둘이 항목은 서버에 있어 동기화로 받으므로 넣지 않는다. 설정은 공유 항목에 걸린 것도 함께 둔다.
 */
@Dao
interface BackupDao {
    /** 지운(soft delete) 항목은 백업하지 않는다 */
    @Query("SELECT * FROM item WHERE owner_user_id = :userId AND visibility = 'PRIVATE' AND deleted_at IS NULL")
    suspend fun getPrivateItems(userId: String): List<ItemEntity>

    @Query("SELECT * FROM user_item_pref WHERE user_id = :userId")
    suspend fun getPrefs(userId: String): List<UserItemPrefEntity>

    @Query("SELECT * FROM item_reminder WHERE user_id = :userId")
    suspend fun getReminders(userId: String): List<ItemReminderEntity>

    /** 이 계정의 나만 보기 항목 · 설정을 백업 내용으로 바꿔치기한다. 공유 항목과 다른 계정 데이터는 그대로 둔다 */
    @Transaction
    suspend fun replace(
        userId: String,
        items: List<ItemEntity>,
        prefs: List<UserItemPrefEntity>,
        reminders: List<ItemReminderEntity>,
    ) {
        deletePrivateItems(userId)
        deletePrefs(userId)
        deleteReminders(userId)
        insertItems(items)
        insertPrefs(prefs)
        insertReminders(reminders)
    }

    @Query("DELETE FROM item WHERE owner_user_id = :userId AND visibility = 'PRIVATE'")
    suspend fun deletePrivateItems(userId: String)

    @Query("DELETE FROM user_item_pref WHERE user_id = :userId")
    suspend fun deletePrefs(userId: String)

    @Query("DELETE FROM item_reminder WHERE user_id = :userId")
    suspend fun deleteReminders(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrefs(prefs: List<UserItemPrefEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ItemReminderEntity>)
}
