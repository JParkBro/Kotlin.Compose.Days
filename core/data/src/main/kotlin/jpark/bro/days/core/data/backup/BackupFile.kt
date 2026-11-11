package jpark.bro.days.core.data.backup

import jpark.bro.days.core.database.entity.ItemEntity
import jpark.bro.days.core.database.entity.ItemReminderEntity
import jpark.bro.days.core.database.entity.SyncState
import jpark.bro.days.core.database.entity.UserItemPrefEntity
import kotlinx.serialization.Serializable

/**
 * 백업 파일 (`backups/{uid}/latest.json`, product-spec 6장). 이 계정의 나만 보기 항목과 사용자별 설정(대표 고정 · 알림).
 * 기기에만 있는 사진은 넣지 않는다. 필드를 바꾸면 [version]을 올리고 이전 버전도 읽게 한다.
 */
@Serializable
internal data class BackupFile(
    val version: Int = CURRENT_VERSION,
    val createdAt: Long,
    val items: List<BackupItem>,
    val prefs: List<BackupPref>,
    val reminders: List<BackupReminder>,
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

@Serializable
internal data class BackupItem(
    val id: String,
    val type: String,
    val title: String,
    val category: String,
    val date: String? = null,
    val countMode: String? = null,
    val countFirstDayAsOne: Boolean = false,
    val repeatRule: String = "NONE",
    val timeSlot: String? = null,
    val time: String? = null,
    val place: String? = null,
    val memo: String? = null,
    val isDone: Boolean = false,
    val showAsDday: Boolean = false,
    val colorKey: String,
    val iconKey: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
internal data class BackupPref(
    val itemId: String,
    val isPinned: Boolean,
    val updatedAt: Long,
)

@Serializable
internal data class BackupReminder(
    val id: String,
    val itemId: String,
    val offsetDays: Int,
    val timeOfDay: String,
)

internal fun ItemEntity.toBackup() =
    BackupItem(
        id = id,
        type = type,
        title = title,
        category = category,
        date = date,
        countMode = countMode,
        countFirstDayAsOne = countFirstDayAsOne,
        repeatRule = repeatRule,
        timeSlot = timeSlot,
        time = time,
        place = place,
        memo = memo,
        isDone = isDone,
        showAsDday = showAsDday,
        colorKey = colorKey,
        iconKey = iconKey,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

/** 복원한 항목은 지금 계정의 나만 보기 항목이다 (서버에 올리지 않으므로 SYNCED, 사진 없음) */
internal fun BackupItem.toEntity(userId: String) =
    ItemEntity(
        id = id,
        type = type,
        ownerUserId = userId,
        coupleId = null,
        visibility = "PRIVATE",
        title = title,
        category = category,
        date = date,
        countMode = countMode,
        countFirstDayAsOne = countFirstDayAsOne,
        repeatRule = repeatRule,
        timeSlot = timeSlot,
        time = time,
        place = place,
        memo = memo,
        isDone = isDone,
        showAsDday = showAsDday,
        colorKey = colorKey,
        iconKey = iconKey,
        backgroundImageId = null,
        updatedBy = userId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        serverUpdatedAt = null,
        deletedAt = null,
        syncState = SyncState.SYNCED.name,
    )

internal fun UserItemPrefEntity.toBackup() = BackupPref(itemId = itemId, isPinned = isPinned, updatedAt = updatedAt)

internal fun BackupPref.toEntity(userId: String) =
    UserItemPrefEntity(userId = userId, itemId = itemId, isPinned = isPinned, updatedAt = updatedAt)

internal fun ItemReminderEntity.toBackup() =
    BackupReminder(id = id, itemId = itemId, offsetDays = offsetDays, timeOfDay = timeOfDay)

internal fun BackupReminder.toEntity(userId: String) =
    ItemReminderEntity(id = id, userId = userId, itemId = itemId, offsetDays = offsetDays, timeOfDay = timeOfDay)
