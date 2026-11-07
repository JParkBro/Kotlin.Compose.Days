package jpark.bro.days.feature.item.impl.itemedit

import jpark.bro.days.core.ads.RewardedAdRequest

internal sealed interface ItemEditEvent {
    /** 저장 · 삭제 · 취소 후, 또는 수정할 항목이 없을 때 */
    data object NavigateBack : ItemEditEvent

    data object SaveFailed : ItemEditEvent

    data object DeleteFailed : ItemEditEvent

    data object OpenPhotoPicker : ItemEditEvent

    /** 고른 사진을 읽거나 줄이지 못함 */
    data object PhotoFailed : ItemEditEvent

    data class ShowRewardedAd(
        val request: RewardedAdRequest,
    ) : ItemEditEvent

    /** 광고를 불러오거나 보여주지 못함 */
    data object RewardedAdFailed : ItemEditEvent

    /** 광고는 봤지만 서버 보상이 아직 반영되지 않음 */
    data object RewardPending : ItemEditEvent

    /** 알림을 하나 켰을 때. 권한이 아직 없으면 요청한다 (Android 13 이상) */
    data object RequestNotificationPermission : ItemEditEvent
}
