package jpark.bro.days.core.data.repository

import jpark.bro.days.core.model.ImageUploadQuota

/**
 * 커플 사진 업로드 한도 (infra-and-billing-protection.md 2-3 · 2-5). 사진을 고르기 전에 확인해,
 * 다 썼으면 보상형 광고를 권하고 광고를 본 뒤 서버(admobReward 함수)가 한도를 올리기를 기다린다.
 */
interface ImageQuotaRepository {
    /** 연결된 커플의 한도 (서버 읽기 1번). 커플이 아니면 null (사진을 서버에 올리지 않으니 한도가 없다) */
    suspend fun uploadQuota(): Result<ImageUploadQuota?>

    /** 보상형 광고를 본 뒤 서버가 bonus를 올릴 때까지 잠깐 기다린다. 제때 오르지 않으면 실패 */
    suspend fun awaitRewardedQuota(quota: ImageUploadQuota): Result<ImageUploadQuota>
}

/**
 * 사진을 고르기 전 확인: 커플 업로드 한도를 다 썼으면 그 한도, 고를 수 있으면 null.
 * 읽지 못하면(오프라인 등) 고르게 둔다. 올릴 때 서버 규칙이 다시 막고, 막히면 그 사진은 이 기기에서만 보인다
 */
suspend fun ImageQuotaRepository.exhaustedQuotaOrNull(): ImageUploadQuota? =
    uploadQuota().getOrNull()?.takeIf { it.remaining == 0 }
