package jpark.bro.days.core.database

import jpark.bro.days.core.database.entity.AnniversaryHideEntity
import jpark.bro.days.core.database.entity.CoupleEntity
import jpark.bro.days.core.database.entity.ItemEntity
import jpark.bro.days.core.database.entity.ItemReminderEntity

/** product-spec.md 9장 솔로 시나리오 (오늘 = 2026.11.05) */
internal const val ME = "user-me"

/** product-spec.md 9장 커플 시나리오의 상대 (지민) */
internal const val PARTNER = "user-partner"
internal const val COUPLE = "couple-1"
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
    syncState = "SYNCED",
)

/** [COUPLE]의 공유 항목 */
internal fun testSharedItem(
    id: String,
    title: String = "지민 생일",
    ownerUserId: String = ME,
    deletedAt: Long? = null,
    syncState: String = "SYNCED",
) = testItem(id = id, title = title, date = "2026-12-20", deletedAt = deletedAt).copy(
    ownerUserId = ownerUserId,
    coupleId = COUPLE,
    visibility = "SHARED",
    updatedBy = ownerUserId,
    syncState = syncState,
)

/** 현우(나, 코드 입력자)와 지민(상대, 코드 발급자)의 커플 */
internal fun testCouple(
    id: String = COUPLE,
    status: String = "ACTIVE",
    member1: String = PARTNER,
    member2: String = ME,
) = CoupleEntity(
    id = id,
    member1UserId = member1,
    member2UserId = member2,
    startDate = "2025-06-14",
    countFirstDayAsOne = true,
    status = status,
    pairCode = "K7Q2MX",
    disconnectedAt = null,
    createdAt = NOW,
    updatedAt = NOW,
    serverUpdatedAt = NOW,
)

internal fun testHide(
    ruleKey: String,
    deletedAt: Long? = null,
    syncState: String = "SYNCED",
) = AnniversaryHideEntity(
    coupleId = COUPLE,
    ruleKey = ruleKey,
    createdAt = NOW,
    deletedAt = deletedAt,
    serverUpdatedAt = NOW,
    syncState = syncState,
)

internal fun testReminder(
    id: String,
    itemId: String,
    offsetDays: Int,
    userId: String = ME,
) = ItemReminderEntity(id = id, userId = userId, itemId = itemId, offsetDays = offsetDays, timeOfDay = "09:00")
