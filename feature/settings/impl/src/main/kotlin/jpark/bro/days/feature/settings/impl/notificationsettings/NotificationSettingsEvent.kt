package jpark.bro.days.feature.settings.impl.notificationsettings

internal sealed interface NotificationSettingsEvent {
    data object NavigateBack : NotificationSettingsEvent
}
