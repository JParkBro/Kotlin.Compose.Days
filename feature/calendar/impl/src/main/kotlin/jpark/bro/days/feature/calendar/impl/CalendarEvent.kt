package jpark.bro.days.feature.calendar.impl

import jpark.bro.days.core.model.ItemType

internal sealed interface CalendarEvent {
    data class OpenItemDetail(
        val id: String,
    ) : CalendarEvent

    data class OpenItemEdit(
        val type: ItemType,
        val date: String?,
    ) : CalendarEvent
}
