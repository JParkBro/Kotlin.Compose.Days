package jpark.bro.days.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 알림 목록 (data-model-erd 3장, Room 전용, 서버로 보내지 않음). 30일이 지나면 지운다.
 * 항목은 지워질 수 있어 FK를 걸지 않고, 보여줄 제목을 기록 당시 값으로 남긴다.
 */
@Entity(
    tableName = "activity_log",
    indices = [Index("user_id", "created_at")],
)
data class ActivityLogEntity(
    @PrimaryKey val id: String,
    /** 이 기록을 보는 계정 (한 기기에 여러 계정이 로그인할 수 있다) */
    @ColumnInfo(name = "user_id") val userId: String,
    /** ActivityType 이름: ITEM_ADDED | ITEM_UPDATED | ITEM_DELETED | REMINDER | ANNIVERSARY */
    val type: String,
    @ColumnInfo(name = "item_id") val itemId: String? = null,
    /** DDAY | PLAN */
    @ColumnInfo(name = "item_type") val itemType: String? = null,
    @ColumnInfo(name = "item_title") val itemTitle: String? = null,
    /** 상대 활동을 한 사람 */
    @ColumnInfo(name = "actor_user_id") val actorUserId: String? = null,
    /** REMINDER: 0 = 당일, 1 = 하루 전, 7 = 일주일 전 */
    @ColumnInfo(name = "offset_days") val offsetDays: Int? = null,
    /** ANNIVERSARY: 자동 기념일 키 (DAY_100 · YEAR_1 · BDAY_{uid}_{연도}) */
    @ColumnInfo(name = "rule_key") val ruleKey: String? = null,
    /** epoch millis */
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "is_read") val isRead: Boolean = false,
)
