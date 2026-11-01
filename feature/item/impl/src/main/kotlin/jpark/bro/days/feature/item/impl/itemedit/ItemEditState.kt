package jpark.bro.days.feature.item.impl.itemedit

import jpark.bro.days.core.model.ItemType
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey

internal data class ItemEditState(
    val itemId: String?,
    val type: ItemType,
    val date: String?,
)
