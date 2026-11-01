package jpark.bro.days.feature.settings.impl.settings


internal sealed interface SettingsEvent {
    data class Navigate(
        val destination: SettingsDestination,
    ) : SettingsEvent
}
