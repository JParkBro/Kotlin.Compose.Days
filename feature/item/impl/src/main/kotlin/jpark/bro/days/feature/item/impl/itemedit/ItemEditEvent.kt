package jpark.bro.days.feature.item.impl.itemedit

internal sealed interface ItemEditEvent {
    data object Saved : ItemEditEvent

    data object NavigateBack : ItemEditEvent
}
