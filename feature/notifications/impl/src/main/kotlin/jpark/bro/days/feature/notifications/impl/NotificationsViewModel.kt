package jpark.bro.days.feature.notifications.impl

import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import jpark.bro.days.feature.notifications.api.NotificationsKey

internal class NotificationsViewModel :
    MviViewModel<NotificationsState, NotificationsAction, NotificationsEvent>(NotificationsState()) {
    override fun onAction(action: NotificationsAction) {
        when (action) {
            is NotificationsAction.ItemClicked -> sendEvent(NotificationsEvent.OpenItemDetail(action.id))
            NotificationsAction.BackClicked -> sendEvent(NotificationsEvent.NavigateBack)
        }
    }
}
