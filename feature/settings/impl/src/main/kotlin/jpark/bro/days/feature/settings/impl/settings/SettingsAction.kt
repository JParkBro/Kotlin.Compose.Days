package jpark.bro.days.feature.settings.impl.settings

import jpark.bro.days.core.model.AppThemeMode
import jpark.bro.days.core.model.AppThemeType

internal sealed interface SettingsAction {
    data class MenuClicked(
        val destination: SettingsDestination,
    ) : SettingsAction

    data class SupportClicked(
        val item: SupportItem,
    ) : SettingsAction

    data object ThemeClicked : SettingsAction

    data object ThemeSheetDismissed : SettingsAction

    data class ThemeTypeSelected(
        val type: AppThemeType,
    ) : SettingsAction

    data class ThemeModeSelected(
        val mode: AppThemeMode,
    ) : SettingsAction

    data object ThemeApplyClicked : SettingsAction
}
