package jpark.bro.days.feature.notifications.impl

import jpark.bro.days.core.model.ItemType
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import jpark.bro.days.feature.notifications.api.NotificationsKey

internal sealed interface NotificationsEvent {
    data class OpenItemDetail(
        val id: String,
    ) : NotificationsEvent

    data object NavigateBack : NotificationsEvent
}
