package jpark.bro.days.core.data.repository

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.session.UserSession
import jpark.bro.days.core.database.dao.CoupleDao
import jpark.bro.days.core.database.dao.UserDao
import jpark.bro.days.core.database.entity.CoupleEntity
import jpark.bro.days.core.database.entity.SyncState
import jpark.bro.days.core.database.entity.UserEntity
import jpark.bro.days.core.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeParseException

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultProfileRepository(
    private val userDao: UserDao,
    private val coupleDao: CoupleDao,
    private val userSession: UserSession,
    private val clock: Clock,
) : ProfileRepository {
    override fun observeMyProfile(): Flow<UserProfile?> =
        userSession.observeUserId().flatMapLatest { userId ->
            if (userId == null) flowOf(null) else userDao.observeUser(userId).map { it?.toModel() }
        }

    override fun observePartnerProfile(): Flow<UserProfile?> =
        userSession
            .observeUserId()
            .flatMapLatest { userId ->
                if (userId ==
                    null
                ) {
                    flowOf(null)
                } else {
                    coupleDao.observeActiveCouple(userId).map { it?.partnerOf(userId) }
                }
            }.distinctUntilChanged()
            .flatMapLatest { partnerId ->
                if (partnerId == null) flowOf(null) else userDao.observeUser(partnerId).map { it?.toModel() }
            }

    override suspend fun saveMyProfile(
        nickname: String,
        birthDate: LocalDate?,
        profileImageId: String?,
    ): Result<UserProfile> =
        suspendRunCatching {
            val trimmed = nickname.trim()
            require(trimmed.isNotEmpty()) { "이름이 비어 있습니다." }
            require(trimmed.length <= UserProfile.NICKNAME_MAX_LENGTH) { "이름이 너무 깁니다." }
            require(birthDate == null || !birthDate.isAfter(LocalDate.now(clock))) { "생일이 오늘 이후입니다." }
            val me = userSession.currentUserId()
            val existing = userDao.getUser(me)
            userDao.upsertUser(
                UserEntity(
                    id = me,
                    nickname = trimmed,
                    birthDate = birthDate?.toString(),
                    updatedAt = clock.millis(),
                    serverUpdatedAt = existing?.serverUpdatedAt,
                    syncState = SyncState.PENDING.name,
                    profileImageId = profileImageId,
                ),
            )
            UserProfile(userId = me, nickname = trimmed, birthDate = birthDate, profileImageId = profileImageId)
        }
}

private fun CoupleEntity.partnerOf(userId: String): String =
    if (member1UserId ==
        userId
    ) {
        member2UserId
    } else {
        member1UserId
    }

/** 상대 기기가 쓴 생일 형식이 틀리면 생일 없음으로 본다 */
private fun UserEntity.toModel(): UserProfile =
    UserProfile(
        userId = id,
        nickname = nickname,
        birthDate =
            birthDate?.let {
                try {
                    LocalDate.parse(it)
                } catch (_: DateTimeParseException) {
                    null
                }
            },
        profileImageId = profileImageId,
    )
