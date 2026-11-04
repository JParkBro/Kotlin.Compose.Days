package jpark.bro.days.core.sync

import jpark.bro.days.core.database.entity.AnniversaryHideEntity
import jpark.bro.days.core.database.entity.CoupleEntity
import jpark.bro.days.core.database.entity.ItemEntity
import jpark.bro.days.core.database.entity.SyncState
import jpark.bro.days.core.firebase.sync.CoupleDto
import jpark.bro.days.core.firebase.sync.HiddenAnniversaryDto
import jpark.bro.days.core.firebase.sync.ItemDto

// Room 엔티티 ↔ Firestore DTO. 받은 것은 서버와 같으므로 SYNCED, 서버 시각은 밀리초로 바꿔 둔다 (커서만 마이크로초)

private const val MICROS_PER_MILLI = 1_000L

internal fun ItemEntity.toDto(coupleId: String): ItemDto =
    ItemDto(
        id = id,
        type = type,
        ownerUserId = ownerUserId,
        coupleId = coupleId,
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
        backgroundImageId = backgroundImageId,
        updatedBy = updatedBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

internal fun ItemDto.toEntity(): ItemEntity =
    ItemEntity(
        id = id,
        type = type,
        ownerUserId = ownerUserId,
        coupleId = coupleId,
        visibility = SHARED,
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
        backgroundImageId = backgroundImageId,
        updatedBy = updatedBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        serverUpdatedAt = serverUpdatedAtMicros / MICROS_PER_MILLI,
        deletedAt = deletedAt,
        syncState = SyncState.SYNCED.name,
    )

/** members[0] 코드 발급자, members[1] 코드 입력자 (CoupleDto가 2명인지 확인했다) */
internal fun CoupleDto.toEntity(): CoupleEntity =
    CoupleEntity(
        id = id,
        member1UserId = members[0],
        member2UserId = members[1],
        startDate = startDate,
        countFirstDayAsOne = countFirstDayAsOne,
        status = status,
        pairCode = pairCode,
        disconnectedAt = disconnectedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        serverUpdatedAt = serverUpdatedAtMicros / MICROS_PER_MILLI,
        syncState = SyncState.SYNCED.name,
    )

internal fun AnniversaryHideEntity.toDto(): HiddenAnniversaryDto =
    HiddenAnniversaryDto(ruleKey = ruleKey, createdAt = createdAt, deletedAt = deletedAt)

internal fun HiddenAnniversaryDto.toEntity(coupleId: String): AnniversaryHideEntity =
    AnniversaryHideEntity(
        coupleId = coupleId,
        ruleKey = ruleKey,
        createdAt = createdAt,
        deletedAt = deletedAt,
        serverUpdatedAt = serverUpdatedAtMicros / MICROS_PER_MILLI,
        syncState = SyncState.SYNCED.name,
    )

private const val SHARED = "SHARED"
