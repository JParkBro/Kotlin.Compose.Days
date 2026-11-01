package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.model.ItemType
import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.home.api.HomeKey
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import jpark.bro.days.feature.notifications.api.NotificationsKey

internal data class HomeState(
    val isLoading: Boolean = true,
)
