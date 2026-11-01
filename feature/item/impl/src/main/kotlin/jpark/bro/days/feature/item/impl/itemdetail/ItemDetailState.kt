package jpark.bro.days.feature.item.impl.itemdetail

import jpark.bro.days.core.model.ItemType

internal data class ItemDetailState(
    val id: String,
    val type: ItemType = ItemType.DDAY,
)
