package jpark.bro.days.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 디데이 · 일정 (data-model-erd.md 2 · 5장).
 * 열거형은 대문자 문자열, 날짜는 yyyy-MM-dd, 시각은 epoch millis. 변환은 core:data 매퍼가 맡는다.
 */
@Entity(
    tableName = "item",
    indices = [Index("couple_id"), Index("date"), Index("sync_state")],
)
data class ItemEntity(
    @PrimaryKey val id: String,
    /** DDAY | PLAN */
    val type: String,
    @ColumnInfo(name = "owner_user_id") val ownerUserId: String,
    /** 공유(SHARED)일 때만 */
    @ColumnInfo(name = "couple_id") val coupleId: String?,
    /** PRIVATE | SHARED */
    val visibility: String,
    val title: String,
    val category: String,
    /** yyyy-MM-dd. PLAN은 null 가능 */
    val date: String?,
    /** DDAY | DPLUS */
    @ColumnInfo(name = "count_mode") val countMode: String?,
    @ColumnInfo(name = "count_first_day_as_one") val countFirstDayAsOne: Boolean = false,
    /** NONE | YEARLY | MONTHLY */
    @ColumnInfo(name = "repeat_rule") val repeatRule: String = "NONE",
    /** ALL_DAY | MORNING | LUNCH | EVENING | EXACT */
    @ColumnInfo(name = "time_slot") val timeSlot: String?,
    /** HH:mm */
    val time: String?,
    val place: String?,
    val memo: String?,
    @ColumnInfo(name = "is_done") val isDone: Boolean = false,
    @ColumnInfo(name = "show_as_dday") val showAsDday: Boolean = false,
    @ColumnInfo(name = "color_key") val colorKey: String,
    @ColumnInfo(name = "icon_key") val iconKey: String,
    @ColumnInfo(name = "background_image_id") val backgroundImageId: String?,
    @ColumnInfo(name = "updated_by") val updatedBy: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "server_updated_at") val serverUpdatedAt: Long?,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
    /** Room 전용. [SyncState.name] */
    @ColumnInfo(name = "sync_state") val syncState: String = SyncState.PENDING.name,
)
