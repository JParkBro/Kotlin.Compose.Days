package jpark.bro.days.core.ads

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * 화면 Root에서 보상형 광고를 띄우는 함수를 만든다. ViewModel의 Event로 [RewardedAdRequest]를 받아 부르고,
 * 결과는 [onResult]로 Action에 넘긴다 (ViewModel은 Activity · 광고 SDK를 모른다).
 */
@Composable
fun rememberRewardedAdLauncher(onResult: (RewardedAdResult) -> Unit): (RewardedAdRequest) -> Unit {
    val activity = LocalActivity.current
    val ads = koinInject<RewardedAds>()
    val scope = rememberCoroutineScope()
    val currentOnResult = rememberUpdatedState(onResult)
    return { request ->
        scope.launch {
            val result = if (activity == null) RewardedAdResult.Failed else ads.show(activity, request)
            currentOnResult.value(result)
        }
    }
}
