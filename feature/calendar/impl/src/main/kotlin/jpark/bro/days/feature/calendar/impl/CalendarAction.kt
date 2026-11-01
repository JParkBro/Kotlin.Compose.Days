package jpark.bro.days.feature.calendar.impl

import jpark.bro.days.core.model.ItemType

internal sealed interface CalendarAction {
    data class ItemClicked(
        val id: String,
    ) : CalendarAction

    data class AddClicked(
        val type: ItemType,
        val date: String?,
    ) : CalendarAction
}
