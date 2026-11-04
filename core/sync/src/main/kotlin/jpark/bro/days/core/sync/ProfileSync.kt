package jpark.bro.days.core.sync

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.database.dao.UserDao
import jpark.bro.days.core.database.entity.SyncState
import jpark.bro.days.core.database.entity.UserEntity
import jpark.bro.days.core.firebase.sync.ProfileDto
import jpark.bro.days.core.firebase.sync.ProfileRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * 프로필 동기화 (`users/{uid}`). [SyncEngine]이 Push · Pull 때 함께 부른다.
 * - 내 프로필: 저장하면 PENDING → 올림. 이 기기에 없으면(새 기기 로그인) 한 번 받아 온다
 * - 상대 프로필: 커플이 연결됐을 때만 받는다 (규칙상 연결 전에는 읽을 수 없음)
 */
class ProfileSync internal constructor(
    private val remote: ProfileRemoteDataSource,
    private val userDao: UserDao,
) {
    fun observeUnsyncedCount(userId: String): Flow<Int> = userDao.observeUnsyncedCount(userId)

    /** 실패하면 FAILED로 두고 false */
    suspend fun push(userId: String): Boolean {
        val user = userDao.getUnsyncedUser(userId) ?: return true
        val ok = suspendRunCatching { remote.pushProfile(user.toDto()) }.isSuccess
        userDao.setSyncState(user.id, user.updatedAt, if (ok) SyncState.SYNCED.name else SyncState.FAILED.name)
        return ok
    }

    /** 이 기기에 내 프로필이 없을 때만 1건 조회한다 (포그라운드마다 읽지 않게) */
    suspend fun pullMineIfMissing(userId: String) {
        if (userDao.getUser(userId) != null) return
        suspendRunCatching { remote.getProfile(userId) }.getOrNull()?.let { userDao.upsertRemoteUser(it.toEntity()) }
    }

    /** 백그라운드 1회성 조회 */
    suspend fun pullPartner(partnerId: String) {
        suspendRunCatching { remote.getProfile(partnerId) }.getOrNull()?.let { userDao.upsertRemoteUser(it.toEntity()) }
    }

    /** 포그라운드 리스너. 오류가 나면 끝나고 다음 포그라운드 진입 때 다시 붙는다 */
    suspend fun listenPartner(partnerId: String) {
        remote
            .observeProfile(partnerId)
            .catch { /* 권한 거부(연결 해제 직후) 등 */ }
            .collect { userDao.upsertRemoteUser(it.toEntity()) }
    }
}

private const val MICROS_PER_MILLI = 1_000L

private fun UserEntity.toDto(): ProfileDto =
    ProfileDto(uid = id, nickname = nickname, birthDate = birthDate, updatedAt = updatedAt)

private fun ProfileDto.toEntity(): UserEntity =
    UserEntity(
        id = uid,
        nickname = nickname,
        birthDate = birthDate,
        updatedAt = updatedAt,
        serverUpdatedAt = serverUpdatedAtMicros / MICROS_PER_MILLI,
        syncState = SyncState.SYNCED.name,
    )
