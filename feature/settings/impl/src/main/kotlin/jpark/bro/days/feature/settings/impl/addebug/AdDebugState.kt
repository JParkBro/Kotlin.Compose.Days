package jpark.bro.days.feature.settings.impl.addebug

import jpark.bro.days.core.model.ImageUploadQuota

internal data class AdDebugState(
    val isLoading: Boolean = false,
    /** 연결된 커플의 사진 한도. 커플이 아니거나 읽지 못했으면 null ([message]로 이유) */
    val quota: ImageUploadQuota? = null,
    /** 광고를 보는 중이거나 보상 반영을 기다리는 중 */
    val isWatching: Boolean = false,
    val message: AdDebugMessage? = null,
)

/** 마지막 동작의 결과 */
internal enum class AdDebugMessage {
    NOT_COUPLED,
    LOAD_FAILED,
    AD_DISMISSED,
    AD_FAILED,
    REWARD_GRANTED,
    REWARD_PENDING,
}
