package jpark.bro.days.feature.settings.impl.widgetsettings

internal sealed interface WidgetSettingsEvent {
    data object NavigateBack : WidgetSettingsEvent

    /** 런처가 추가 요청을 받지 못함 */
    data object AddFailed : WidgetSettingsEvent
}
