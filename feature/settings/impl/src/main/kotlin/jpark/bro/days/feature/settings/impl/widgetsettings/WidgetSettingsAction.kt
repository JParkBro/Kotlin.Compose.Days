package jpark.bro.days.feature.settings.impl.widgetsettings

import jpark.bro.days.core.model.WidgetSize

internal sealed interface WidgetSettingsAction {
    data object BackClicked : WidgetSettingsAction

    /** 그 위젯의 설정 화면을 연다 */
    data class WidgetClicked(
        val widgetId: Int,
    ) : WidgetSettingsAction

    data object AddClicked : WidgetSettingsAction

    data class SizeSelected(
        val size: WidgetSize,
    ) : WidgetSettingsAction

    data object SizeSheetDismissed : WidgetSettingsAction
}
