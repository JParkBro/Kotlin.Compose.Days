package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.model.ItemType

internal sealed interface HomeEvent {
    data class OpenItemDetail(
        val id: String,
    ) : HomeEvent

    data class OpenItemEdit(
        val type: ItemType,
    ) : HomeEvent

    data object OpenNotifications : HomeEvent

    data object OpenPairing : HomeEvent
}
