package jpark.bro.days.feature.item.impl.itemedit

internal sealed interface ItemEditAction {
    data object SaveClicked : ItemEditAction

    data object CancelClicked : ItemEditAction
}
