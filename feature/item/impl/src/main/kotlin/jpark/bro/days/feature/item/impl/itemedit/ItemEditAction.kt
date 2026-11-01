package jpark.bro.days.feature.item.impl.itemedit

import jpark.bro.days.core.model.ItemType
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey

internal sealed interface ItemEditAction {
    data object SaveClicked : ItemEditAction

    data object CancelClicked : ItemEditAction
}
