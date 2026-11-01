package jpark.bro.days.feature.item.impl.itemedit

import jpark.bro.days.core.model.ItemType

internal data class ItemEditState(
    val itemId: String?,
    val type: ItemType,
    val date: String?,
)
