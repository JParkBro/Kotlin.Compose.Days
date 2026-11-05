package jpark.bro.days.core.data.couple

import jpark.bro.days.core.database.entity.CoupleEntity
import jpark.bro.days.core.database.entity.SyncState
import jpark.bro.days.core.firebase.sync.CoupleDto
import jpark.bro.days.core.model.Couple
import java.time.LocalDate

internal const val ACTIVE = "ACTIVE"
internal const val DISCONNECTED = "DISCONNECTED"
private const val MICROS_PER_MILLI = 1_000L

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

internal fun CoupleEntity.toDto(): CoupleDto =
    CoupleDto(
        id = id,
        members = listOf(member1UserId, member2UserId),
        startDate = startDate,
        countFirstDayAsOne = countFirstDayAsOne,
        status = status,
        pairCode = pairCode,
        disconnectedAt = disconnectedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun CoupleEntity.partnerOf(userId: String): String =
    if (member1UserId ==
        userId
    ) {
        member2UserId
    } else {
        member1UserId
    }

internal fun CoupleEntity.toModel(userId: String): Couple =
    Couple(
        id = id,
        partnerId = partnerOf(userId),
        startDate = LocalDate.parse(startDate),
        countFirstDayAsOne = countFirstDayAsOne,
    )
