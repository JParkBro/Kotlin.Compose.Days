package jpark.bro.days.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * 사용자별 항목 설정 (홈 대표 고정). 공유하지 않는다.
 * item은 soft delete라 행이 지워지지 않고, 동기화로 늦게 도착할 수 있어 FK를 걸지 않는다.
 */
@Entity(
    tableName = "user_item_pref",
    primaryKeys = ["user_id", "item_id"],
    indices = [Index("item_id")],
)
data class UserItemPrefEntity(
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "item_id") val itemId: String,
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean = false,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
