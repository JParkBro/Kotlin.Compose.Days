package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.ImageQuotaRepository
import jpark.bro.days.core.model.ImageUploadQuota

/** 커플 사진 한도 Fake. [quota]가 null이면 커플이 아님 */
class FakeImageQuotaRepository(
    var quota: ImageUploadQuota? = null,
) : ImageQuotaRepository {
    /** true면 한도 읽기가 실패한다 (오프라인 등) */
    var failQuota: Boolean = false

    /** false면 보상 반영을 기다리다 실패한다 (콜백이 늦음) */
    var grantReward: Boolean = true

    override suspend fun uploadQuota(): Result<ImageUploadQuota?> =
        if (failQuota) Result.failure(IllegalStateException("한도 읽기 실패 (테스트)")) else Result.success(quota)

    override suspend fun awaitRewardedQuota(quota: ImageUploadQuota): Result<ImageUploadQuota> {
        if (!grantReward) return Result.failure(IllegalStateException("보상 미반영 (테스트)"))
        val rewarded = quota.copy(bonus = quota.bonus + 1)
        this.quota = rewarded
        return Result.success(rewarded)
    }
}
