package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.model.ItemType
import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.home.api.HomeKey
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import jpark.bro.days.feature.notifications.api.NotificationsKey

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
