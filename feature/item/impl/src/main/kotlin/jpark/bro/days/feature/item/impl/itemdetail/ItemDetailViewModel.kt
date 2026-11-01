package jpark.bro.days.feature.item.impl.itemdetail

import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.item.api.ItemDetailKey

internal class ItemDetailViewModel(
    key: ItemDetailKey,
) : MviViewModel<ItemDetailState, ItemDetailAction, ItemDetailEvent>(ItemDetailState(id = key.id)) {
    override fun onAction(action: ItemDetailAction) {
        when (action) {
            ItemDetailAction.EditClicked -> sendEvent(ItemDetailEvent.OpenEdit(currentState.id, currentState.type))
            ItemDetailAction.BackClicked -> sendEvent(ItemDetailEvent.NavigateBack)
        }
    }
}
