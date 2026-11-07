package jpark.bro.days.core.model

/**
 * 커플 사진 업로드 한도 (infra-and-billing-protection.md 2-3): 누적 [BASE_LIMIT]번 + 보상형 광고로 받은 bonus.
 * [userId] · [coupleId]는 보상형 광고의 서버 측 확인 값으로도 쓴다.
 */
data class ImageUploadQuota(
    val userId: String,
    val coupleId: String,
    val used: Int,
    val bonus: Int,
) {
    val limit: Int get() = BASE_LIMIT + bonus

    val remaining: Int get() = (limit - used).coerceAtLeast(0)

    /** 광고로 더 늘릴 수 있는지 (서버 상한, admobReward 함수와 같게) */
    val canEarnBonus: Boolean get() = bonus < BONUS_LIMIT

    companion object {
        const val BASE_LIMIT = 30
        const val BONUS_LIMIT = 30
    }
}
