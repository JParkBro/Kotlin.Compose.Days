package jpark.bro.days.feature.settings.impl.settings


internal sealed interface SettingsAction {
    data class MenuClicked(
        val destination: SettingsDestination,
    ) : SettingsAction
}
