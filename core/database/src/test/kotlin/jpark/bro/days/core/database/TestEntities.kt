package jpark.bro.days.core.database

import jpark.bro.days.core.database.entity.ItemEntity
import jpark.bro.days.core.database.entity.ItemReminderEntity

/** product-spec.md 9장 솔로 시나리오 (오늘 = 2026.11.05) */
internal const val ME = "user-me"
internal const val NOW = 1_793_836_800_000L // 2026-11-05T00:00:00Z

internal fun testItem(
    id: String,
    title: String = "전역일",
    date: String? = "2027-03-19",
    createdAt: Long = NOW,
    deletedAt: Long? = null,
) = ItemEntity(
    id = id,
    type = "DDAY",
    ownerUserId = ME,
    coupleId = null,
    visibility = "PRIVATE",
    title = title,
    category = "ETC",
    date = date,
    countMode = "DDAY",
    timeSlot = null,
    time = null,
    place = null,
    memo = null,
    colorKey = "primary",
    iconKey = "star",
    backgroundImageId = null,
    updatedBy = ME,
    createdAt = createdAt,
    updatedAt = createdAt,
    serverUpdatedAt = null,
    deletedAt = deletedAt,
)

internal fun testReminder(
    id: String,
    itemId: String,
    offsetDays: Int,
    userId: String = ME,
) = ItemReminderEntity(id = id, userId = userId, itemId = itemId, offsetDays = offsetDays, timeOfDay = "09:00")
