package jpark.bro.days.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * 숨긴 자동 기념일 (data-model-erd.md 2장 `rule_key`: DAY_600, YEAR_2, BDAY_{uid}_{연도}).
 * 자동 기념일은 계산하고 숨긴 것만 저장한다. 숨김 해제도 [deletedAt] 기록(soft delete)이라 상대 기기에 전달된다.
 */
@Entity(tableName = "anniversary_hide", primaryKeys = ["couple_id", "rule_key"])
data class AnniversaryHideEntity(
    @ColumnInfo(name = "couple_id") val coupleId: String,
    @ColumnInfo(name = "rule_key") val ruleKey: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
    @ColumnInfo(name = "server_updated_at") val serverUpdatedAt: Long?,
    /** Room 전용. [SyncState.name] */
    @ColumnInfo(name = "sync_state") val syncState: String = SyncState.PENDING.name,
)
