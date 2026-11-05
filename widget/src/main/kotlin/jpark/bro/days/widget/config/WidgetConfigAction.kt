package jpark.bro.days.widget.config

import jpark.bro.days.core.model.WidgetBackground

internal sealed interface WidgetConfigAction {
    /** null이면 자동 */
    data class ItemSelected(
        val itemId: String?,
    ) : WidgetConfigAction

    data class BackgroundSelected(
        val background: WidgetBackground,
    ) : WidgetConfigAction

    data class ShowProfileChanged(
        val show: Boolean,
    ) : WidgetConfigAction

    data object SaveClicked : WidgetConfigAction

    data object CloseClicked : WidgetConfigAction
}
