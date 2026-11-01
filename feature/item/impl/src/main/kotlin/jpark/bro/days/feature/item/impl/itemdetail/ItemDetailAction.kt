package jpark.bro.days.feature.item.impl.itemdetail

internal sealed interface ItemDetailAction {
    data object EditClicked : ItemDetailAction

    data object BackClicked : ItemDetailAction
}
