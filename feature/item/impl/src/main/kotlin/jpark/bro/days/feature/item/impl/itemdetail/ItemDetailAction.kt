package jpark.bro.days.feature.item.impl.itemdetail

import jpark.bro.days.core.model.ItemType
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey

internal sealed interface ItemDetailAction {
    data object EditClicked : ItemDetailAction

    data object BackClicked : ItemDetailAction
}
