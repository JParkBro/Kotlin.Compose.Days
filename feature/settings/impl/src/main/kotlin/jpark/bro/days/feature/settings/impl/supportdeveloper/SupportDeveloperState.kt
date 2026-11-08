package jpark.bro.days.feature.settings.impl.supportdeveloper

import jpark.bro.days.core.billing.SupportOffer

internal data class SupportDeveloperState(
    val isLoading: Boolean = true,
    /** Play에 등록된 상품. 결제를 쓸 수 없거나 아직 등록 전이면 비어 있다 */
    val offers: List<SupportOffer> = emptyList(),
    /** 상품을 불러오지 못함 (Play 연결 실패) */
    val isLoadFailed: Boolean = false,
    /** 결제 창을 띄운 중 */
    val isPurchasing: Boolean = false,
)
