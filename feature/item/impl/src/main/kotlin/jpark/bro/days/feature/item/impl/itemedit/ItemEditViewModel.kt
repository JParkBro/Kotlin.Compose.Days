package jpark.bro.days.feature.item.impl.itemedit

import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey

internal class ItemEditViewModel(
    key: ItemEditKey,
) : MviViewModel<ItemEditState, ItemEditAction, ItemEditEvent>(
        ItemEditState(itemId = key.itemId, type = key.type, date = key.presetDate),
    ) {
    override fun onAction(action: ItemEditAction) {
        when (action) {
            ItemEditAction.SaveClicked -> sendEvent(ItemEditEvent.Saved)
            ItemEditAction.CancelClicked -> sendEvent(ItemEditEvent.NavigateBack)
        }
    }
}
