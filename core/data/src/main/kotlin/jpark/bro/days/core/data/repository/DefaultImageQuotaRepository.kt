package jpark.bro.days.core.data.repository

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.session.UserSession
import jpark.bro.days.core.database.dao.CoupleDao
import jpark.bro.days.core.firebase.sync.ImageRemoteDataSource
import jpark.bro.days.core.firebase.sync.ImageUsage
import jpark.bro.days.core.model.ImageUploadQuota

internal class DefaultImageQuotaRepository(
    private val coupleDao: CoupleDao,
    private val remote: ImageRemoteDataSource,
    private val userSession: UserSession,
) : ImageQuotaRepository {
    override suspend fun uploadQuota(): Result<ImageUploadQuota?> =
        suspendRunCatching {
            val me = userSession.currentUserId()
            val couple = coupleDao.getActiveCouple(me) ?: return@suspendRunCatching null
            remote.usage(couple.id).toQuota(me, couple.id)
        }

    override suspend fun awaitRewardedQuota(quota: ImageUploadQuota): Result<ImageUploadQuota> =
        suspendRunCatching {
            val usage = remote.awaitBonusAbove(quota.coupleId, quota.bonus, REWARD_TIMEOUT_MILLIS)
            checkNotNull(usage) { "보상이 아직 반영되지 않았습니다." }.toQuota(quota.userId, quota.coupleId)
        }

    private fun ImageUsage.toQuota(
        userId: String,
        coupleId: String,
    ) = ImageUploadQuota(userId = userId, coupleId = coupleId, used = count, bonus = bonus)

    private companion object {
        /** AdMob SSV 콜백은 보통 몇 초 안에 온다 */
        const val REWARD_TIMEOUT_MILLIS = 20_000L
    }
}
