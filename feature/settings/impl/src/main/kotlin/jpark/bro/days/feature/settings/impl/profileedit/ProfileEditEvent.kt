package jpark.bro.days.feature.settings.impl.profileedit

import jpark.bro.days.core.ads.RewardedAdRequest

internal sealed interface ProfileEditEvent {
    /** 저장하고 이전 화면으로 */
    data object Saved : ProfileEditEvent

    data object SaveFailed : ProfileEditEvent

    data object NavigateBack : ProfileEditEvent

    data object OpenPhotoPicker : ProfileEditEvent

    /** 고른 사진을 읽거나 줄이지 못함 */
    data object PhotoFailed : ProfileEditEvent

    data class ShowRewardedAd(
        val request: RewardedAdRequest,
    ) : ProfileEditEvent

    /** 광고를 불러오거나 보여주지 못함 */
    data object RewardedAdFailed : ProfileEditEvent

    /** 광고는 봤지만 서버 보상이 아직 반영되지 않음 */
    data object RewardPending : ProfileEditEvent
}
