package jpark.bro.days.feature.settings.impl.settings

internal sealed interface SettingsEvent {
    data class Navigate(
        val destination: SettingsDestination,
    ) : SettingsEvent

    data class OpenSupport(
        val item: SupportItem,
    ) : SettingsEvent

    data object ThemeSaveFailed : SettingsEvent
}
