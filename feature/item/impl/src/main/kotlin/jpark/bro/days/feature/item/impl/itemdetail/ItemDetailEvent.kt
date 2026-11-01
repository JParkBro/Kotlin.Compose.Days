package jpark.bro.days.feature.item.impl.itemdetail

import jpark.bro.days.core.model.ItemType

internal sealed interface ItemDetailEvent {
    data class OpenEdit(
        val id: String,
        val type: ItemType,
    ) : ItemDetailEvent

    data object NavigateBack : ItemDetailEvent
}
