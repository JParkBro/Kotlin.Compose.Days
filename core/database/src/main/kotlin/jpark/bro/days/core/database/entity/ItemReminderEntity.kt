package jpark.bro.days.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 사용자별 항목 알림. 공유하지 않는다. FK를 걸지 않는 이유는 [UserItemPrefEntity]와 같다. */
@Entity(
    tableName = "item_reminder",
    indices = [Index("user_id", "item_id")],
)
data class ItemReminderEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "item_id") val itemId: String,
    /** 0 = 당일, 1 = 하루 전, 7 = 일주일 전 */
    @ColumnInfo(name = "offset_days") val offsetDays: Int,
    /** HH:mm */
    @ColumnInfo(name = "time_of_day") val timeOfDay: String,
)
