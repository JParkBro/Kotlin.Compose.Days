package jpark.bro.days.feature.item.impl.itemdetail

import jpark.bro.days.core.model.ItemType

internal sealed interface ItemDetailEvent {
    data class OpenEdit(
        val id: String,
        val type: ItemType,
    ) : ItemDetailEvent

    data object NavigateBack : ItemDetailEvent

    /** 런처가 위젯 추가 요청을 받지 못함 */
    data object WidgetAddFailed : ItemDetailEvent
}
