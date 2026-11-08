package jpark.bro.days.feature.settings.impl.supportdeveloper

import jpark.bro.days.core.billing.SupportProduct
import jpark.bro.days.core.billing.SupportPurchaseResult

internal sealed interface SupportDeveloperAction {
    data class ProductClicked(
        val product: SupportProduct,
    ) : SupportDeveloperAction

    data class PurchaseFinished(
        val result: SupportPurchaseResult,
    ) : SupportDeveloperAction

    /** 상품을 불러오지 못했을 때 다시 시도 */
    data object RetryClicked : SupportDeveloperAction

    data object BackClicked : SupportDeveloperAction
}
