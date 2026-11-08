package jpark.bro.days.feature.settings.impl.addebug

import jpark.bro.days.core.ads.RewardedAdResult

internal sealed interface AdDebugAction {
    /** 서버의 사진 한도를 다시 읽는다 */
    data object RefreshClicked : AdDebugAction

    /** 한도가 남아 있어도 보상형 광고를 띄운다 (SSV 콜백 확인용) */
    data object WatchAdClicked : AdDebugAction

    data class RewardedAdFinished(
        val result: RewardedAdResult,
    ) : AdDebugAction
}
