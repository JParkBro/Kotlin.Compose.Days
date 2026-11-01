package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.model.ItemType
import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.home.api.HomeKey
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import jpark.bro.days.feature.notifications.api.NotificationsKey

internal sealed interface HomeAction {
    data class ItemClicked(
        val id: String,
    ) : HomeAction

    data class AddClicked(
        val type: ItemType,
    ) : HomeAction

    data object NotificationsClicked : HomeAction

    data object ConnectCoupleClicked : HomeAction
}
