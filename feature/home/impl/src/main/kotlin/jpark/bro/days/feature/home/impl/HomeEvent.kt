package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.model.ItemType

internal sealed interface HomeEvent {
    data class OpenItemDetail(
        val id: String,
    ) : HomeEvent

    /** [itemId]가 있으면 수정, 없으면 추가. [quickStart]가 있으면 카테고리 · 제목을 채워서 연다 */
    data class OpenItemEdit(
        val type: ItemType,
        val itemId: String? = null,
        val quickStart: QuickStart? = null,
    ) : HomeEvent

    data object OpenNotifications : HomeEvent

    data object OpenPairing : HomeEvent
}
