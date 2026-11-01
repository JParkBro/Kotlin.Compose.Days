package jpark.bro.days.feature.settings.impl.widgetsettings

internal sealed interface WidgetSettingsAction {
    data object BackClicked : WidgetSettingsAction
}
