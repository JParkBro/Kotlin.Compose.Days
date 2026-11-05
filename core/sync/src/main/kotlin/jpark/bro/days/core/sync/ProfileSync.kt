package jpark.bro.days.core.sync

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.database.dao.ImageSyncDao
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
 * - 프로필 사진: 연결된 커플에 올린 사진의 id만 적는다 (솔로 · 올리기 전에는 null, 사진은 [ImageSync]가 먼저 올림)
 */
class ProfileSync internal constructor(
    private val remote: ProfileRemoteDataSource,
    private val userDao: UserDao,
    private val imageDao: ImageSyncDao,
) {
    fun observeUnsyncedCount(userId: String): Flow<Int> = userDao.observeUnsyncedCount(userId)

    /** 실패하면 FAILED로 두고 false */
    suspend fun push(userId: String): Boolean {
        val user = userDao.getUnsyncedUser(userId) ?: return true
        val imageId = user.profileImageId?.takeIf { imageDao.isUploadedToActiveCouple(it, userId) }
        val ok = suspendRunCatching { remote.pushProfile(user.toDto().copy(profileImageId = imageId)) }.isSuccess
        userDao.setSyncState(user.id, user.updatedAt, if (ok) SyncState.SYNCED.name else SyncState.FAILED.name)
        return ok
    }

    /** 이 기기에 내 프로필이 없을 때만 1건 조회한다 (포그라운드마다 읽지 않게) */
    suspend fun pullMineIfMissing(userId: String) {
        if (userDao.getUser(userId) != null) return
        suspendRunCatching { remote.getProfile(userId) }.getOrNull()?.let { apply(it) }
    }

    /** 백그라운드 1회성 조회 */
    suspend fun pullPartner(partnerId: String) {
        suspendRunCatching { remote.getProfile(partnerId) }.getOrNull()?.let { apply(it) }
    }

    /** 포그라운드 리스너. 오류가 나면 끝나고 다음 포그라운드 진입 때 다시 붙는다 */
    suspend fun listenPartner(partnerId: String) {
        remote
            .observeProfile(partnerId)
            .catch { /* 권한 거부(연결 해제 직후) 등 */ }
            .collect { apply(it) }
    }

    /** 받은 프로필 반영. 프로필 사진은 화면에 보일 때 받도록 알아 둔다 */
    private suspend fun apply(profile: ProfileDto) {
        val imageId = profile.profileImageId
        val coupleId = profile.coupleId
        if (imageId != null && coupleId != null) imageDao.registerRemoteImage(imageId, profile.uid, coupleId)
        userDao.upsertRemoteUser(profile.toEntity())
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
        profileImageId = profileImageId,
    )
