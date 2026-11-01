package jpark.bro.days.feature.settings.impl.widgetsettings

internal sealed interface WidgetSettingsEvent {
    data object NavigateBack : WidgetSettingsEvent
}
