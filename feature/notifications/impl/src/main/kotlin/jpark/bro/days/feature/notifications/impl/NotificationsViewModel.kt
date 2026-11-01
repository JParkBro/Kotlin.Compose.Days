package jpark.bro.days.feature.notifications.impl

import jpark.bro.days.core.ui.MviViewModel

internal class NotificationsViewModel :
    MviViewModel<NotificationsState, NotificationsAction, NotificationsEvent>(NotificationsState()) {
    override fun onAction(action: NotificationsAction) {
        when (action) {
            is NotificationsAction.ItemClicked -> sendEvent(NotificationsEvent.OpenItemDetail(action.id))
            NotificationsAction.BackClicked -> sendEvent(NotificationsEvent.NavigateBack)
        }
    }
}
