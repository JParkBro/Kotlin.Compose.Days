package jpark.bro.days.feature.notifications.impl

import jpark.bro.days.core.model.ItemType
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import jpark.bro.days.feature.notifications.api.NotificationsKey

internal sealed interface NotificationsAction {
    data class ItemClicked(
        val id: String,
    ) : NotificationsAction

    data object BackClicked : NotificationsAction
}
