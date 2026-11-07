package jpark.bro.days.feature.settings.impl.addebug

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.ads.RewardedAdRequest
import jpark.bro.days.core.ads.RewardedAdResult
import jpark.bro.days.core.data.repository.ImageQuotaRepository
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.launch

/**
 * 디버그 빌드에서만 보이는 설정의 "광고 테스트" 섹션: 커플 사진 한도(count · bonus)를 보고,
 * 한도와 상관없이 보상형 광고를 띄워 admobReward 콜백이 bonus를 올리는지 확인한다.
 * 화면이 열릴 때 서버 읽기 1번, 새로고침마다 1번.
 */
internal class AdDebugViewModel(
    private val quotaRepository: ImageQuotaRepository,
) : MviViewModel<AdDebugState, AdDebugAction, AdDebugEvent>(AdDebugState()) {
    init {
        refresh()
    }

    override fun onAction(action: AdDebugAction) {
        when (action) {
            AdDebugAction.RefreshClicked -> refresh()
            AdDebugAction.WatchAdClicked -> watchAd()
            is AdDebugAction.RewardedAdFinished -> onRewardedAdFinished(action.result)
        }
    }

    private fun refresh() {
        if (currentState.isLoading || currentState.isWatching) return
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            quotaRepository
                .uploadQuota()
                .onSuccess { quota ->
                    setState {
                        copy(
                            isLoading = false,
                            quota = quota,
                            message =
                                if (quota ==
                                    null
                                ) {
                                    AdDebugMessage.NOT_COUPLED
                                } else {
                                    null
                                },
                        )
                    }
                }.onFailure {
                    setState { copy(isLoading = false, quota = null, message = AdDebugMessage.LOAD_FAILED) }
                }
        }
    }

    private fun watchAd() {
        val quota = currentState.quota
        if (quota == null || currentState.isWatching) return
        setState { copy(isWatching = true, message = null) }
        sendEvent(AdDebugEvent.ShowRewardedAd(RewardedAdRequest(quota.userId, quota.coupleId)))
    }

    private fun onRewardedAdFinished(result: RewardedAdResult) {
        val quota = currentState.quota
        if (quota == null || result != RewardedAdResult.Earned) {
            val message =
                if (result ==
                    RewardedAdResult.Failed
                ) {
                    AdDebugMessage.AD_FAILED
                } else {
                    AdDebugMessage.AD_DISMISSED
                }
            setState { copy(isWatching = false, message = message) }
            return
        }
        viewModelScope.launch {
            quotaRepository
                .awaitRewardedQuota(quota)
                .onSuccess { rewarded ->
                    setState { copy(isWatching = false, quota = rewarded, message = AdDebugMessage.REWARD_GRANTED) }
                }.onFailure {
                    setState { copy(isWatching = false, message = AdDebugMessage.REWARD_PENDING) }
                }
        }
    }
}
