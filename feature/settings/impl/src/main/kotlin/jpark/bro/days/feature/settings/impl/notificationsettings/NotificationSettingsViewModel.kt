package jpark.bro.days.feature.settings.impl.notificationsettings

import jpark.bro.days.core.ui.MviViewModel

internal class NotificationSettingsViewModel :
    MviViewModel<NotificationSettingsState, NotificationSettingsAction, NotificationSettingsEvent>(
        NotificationSettingsState(),
    ) {
    override fun onAction(action: NotificationSettingsAction) {
        when (action) {
            NotificationSettingsAction.BackClicked -> sendEvent(NotificationSettingsEvent.NavigateBack)
        }
    }
}
