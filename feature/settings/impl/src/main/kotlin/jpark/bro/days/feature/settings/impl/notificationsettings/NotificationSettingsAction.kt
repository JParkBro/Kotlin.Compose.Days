package jpark.bro.days.feature.settings.impl.notificationsettings

internal sealed interface NotificationSettingsAction {
    data object BackClicked : NotificationSettingsAction
}
