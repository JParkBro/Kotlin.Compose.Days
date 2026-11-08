package jpark.bro.days.feature.settings.impl.supportdeveloper

import jpark.bro.days.core.billing.SupportProduct

internal sealed interface SupportDeveloperEvent {
    data class LaunchPurchase(
        val product: SupportProduct,
    ) : SupportDeveloperEvent

    data object Thanks : SupportDeveloperEvent

    /** 결제가 보류됨. 결제되면 다음에 열 때 처리한다 */
    data object PurchasePending : SupportDeveloperEvent

    data object PurchaseFailed : SupportDeveloperEvent

    data object NavigateBack : SupportDeveloperEvent
}
