package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.ui.MviViewModel

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
