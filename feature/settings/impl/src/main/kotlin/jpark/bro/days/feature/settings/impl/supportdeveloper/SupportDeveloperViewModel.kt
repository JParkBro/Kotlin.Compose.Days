package jpark.bro.days.feature.settings.impl.supportdeveloper

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.billing.SupportPurchaseResult
import jpark.bro.days.core.billing.SupportStore
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.launch

/**
 * 개발자 응원하기 (ads-strategy.md 6장): 혜택 없는 소모형 상품 3종. 열 때 Play 상품 · 가격을 읽고,
 * 결제됐지만 소모하지 못한 후원(결제 중 앱 종료 · 보류 후 결제)을 먼저 소모한다. 서버는 쓰지 않는다.
 */
internal class SupportDeveloperViewModel(
    private val store: SupportStore,
) : MviViewModel<SupportDeveloperState, SupportDeveloperAction, SupportDeveloperEvent>(SupportDeveloperState()) {
    init {
        viewModelScope.launch { store.consumePending() }
        load()
    }

    override fun onAction(action: SupportDeveloperAction) {
        when (action) {
            is SupportDeveloperAction.ProductClicked -> {
                if (currentState.isPurchasing) return
                setState { copy(isPurchasing = true) }
                sendEvent(SupportDeveloperEvent.LaunchPurchase(action.product))
            }

            is SupportDeveloperAction.PurchaseFinished -> {
                setState { copy(isPurchasing = false) }
                when (action.result) {
                    SupportPurchaseResult.Purchased -> sendEvent(SupportDeveloperEvent.Thanks)
                    SupportPurchaseResult.Pending -> sendEvent(SupportDeveloperEvent.PurchasePending)
                    SupportPurchaseResult.Failed -> sendEvent(SupportDeveloperEvent.PurchaseFailed)
                    SupportPurchaseResult.Canceled -> Unit
                }
            }

            SupportDeveloperAction.RetryClicked -> {
                load()
            }

            SupportDeveloperAction.BackClicked -> {
                sendEvent(SupportDeveloperEvent.NavigateBack)
            }
        }
    }

    private fun load() {
        setState { copy(isLoading = true, isLoadFailed = false) }
        viewModelScope.launch {
            store
                .offers()
                .onSuccess { offers -> setState { copy(isLoading = false, offers = offers) } }
                .onFailure { setState { copy(isLoading = false, offers = emptyList(), isLoadFailed = true) } }
        }
    }
}
