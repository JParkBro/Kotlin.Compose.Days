package jpark.bro.days.feature.item.impl.itemdetail

import jpark.bro.days.core.model.WidgetSize

internal sealed interface ItemDetailAction {
    data object EditClicked : ItemDetailAction

    data object BackClicked : ItemDetailAction

    /** 크기 고르기 시트를 연다 */
    data object AddWidgetClicked : ItemDetailAction

    data class WidgetSizeSelected(
        val size: WidgetSize,
    ) : ItemDetailAction

    data object WidgetSheetDismissed : ItemDetailAction
}
