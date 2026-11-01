package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.home.api.HomeKey
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import jpark.bro.days.feature.notifications.api.NotificationsKey

internal class HomeViewModel : MviViewModel<HomeState, HomeAction, HomeEvent>(HomeState()) {
    override fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.ItemClicked -> sendEvent(HomeEvent.OpenItemDetail(action.id))
            is HomeAction.AddClicked -> sendEvent(HomeEvent.OpenItemEdit(action.type))
            HomeAction.NotificationsClicked -> sendEvent(HomeEvent.OpenNotifications)
            HomeAction.ConnectCoupleClicked -> sendEvent(HomeEvent.OpenPairing)
        }
    }
}
