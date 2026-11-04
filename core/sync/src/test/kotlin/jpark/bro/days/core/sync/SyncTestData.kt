package jpark.bro.days.core.sync

import jpark.bro.days.core.database.entity.AnniversaryHideEntity
import jpark.bro.days.core.database.entity.ItemEntity
import jpark.bro.days.core.firebase.sync.CoupleDto
import jpark.bro.days.core.firebase.sync.HiddenAnniversaryDto
import jpark.bro.days.core.firebase.sync.ItemDto

/** product-spec.md 9장 커플 시나리오: 현우(나) · 지민(상대), 오늘 = 2026.11.05 */
internal const val ME = "user-me"
internal const val PARTNER = "user-partner"
internal const val COUPLE = "couple-1"
internal const val NOW = 1_793_836_800_000L

internal fun coupleDto(status: String = "ACTIVE") =
    CoupleDto(
        id = COUPLE,
        members = listOf(PARTNER, ME),
        startDate = "2025-06-14",
        countFirstDayAsOne = true,
        status = status,
        pairCode = "K7Q2MX",
        disconnectedAt = null,
        createdAt = NOW,
        updatedAt = NOW,
        serverUpdatedAtMicros = NOW * 1_000,
    )

internal fun itemDto(
    id: String,
    title: String = "지민 생일",
    serverMicros: Long = NOW * 1_000,
    coupleId: String = COUPLE,
) = ItemDto(
    id = id,
    type = "DDAY",
    ownerUserId = PARTNER,
    coupleId = coupleId,
    title = title,
    category = "BIRTHDAY",
    date = "2026-12-20",
    countMode = "DDAY",
    countFirstDayAsOne = false,
    repeatRule = "YEARLY",
    timeSlot = null,
    time = null,
    place = null,
    memo = null,
    isDone = false,
    showAsDday = false,
    colorKey = "primary",
    iconKey = "cake",
    backgroundImageId = null,
    updatedBy = PARTNER,
    createdAt = NOW,
    updatedAt = NOW,
    deletedAt = null,
    serverUpdatedAtMicros = serverMicros,
)

/** 내가 저장한 공유 항목 (Room) */
internal fun mySharedItem(
    id: String,
    syncState: String = "PENDING",
): ItemEntity =
    itemDto(id).toEntity().copy(ownerUserId = ME, updatedBy = ME, serverUpdatedAt = null, syncState = syncState)

internal fun myHide(ruleKey: String): AnniversaryHideEntity =
    HiddenAnniversaryDto(ruleKey = ruleKey, createdAt = NOW, deletedAt = null)
        .toEntity(COUPLE)
        .copy(serverUpdatedAt = null, syncState = "PENDING")
