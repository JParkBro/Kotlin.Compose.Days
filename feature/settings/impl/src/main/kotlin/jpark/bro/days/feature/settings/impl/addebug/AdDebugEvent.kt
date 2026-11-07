package jpark.bro.days.feature.settings.impl.addebug

import jpark.bro.days.core.ads.RewardedAdRequest

internal sealed interface AdDebugEvent {
    data class ShowRewardedAd(
        val request: RewardedAdRequest,
    ) : AdDebugEvent
}
