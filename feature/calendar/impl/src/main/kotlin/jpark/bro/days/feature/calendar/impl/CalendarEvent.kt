package jpark.bro.days.feature.calendar.impl

import jpark.bro.days.core.model.ItemType
import jpark.bro.days.feature.calendar.api.CalendarKey
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey

internal sealed interface CalendarEvent {
    data class OpenItemDetail(
        val id: String,
    ) : CalendarEvent

    data class OpenItemEdit(
        val type: ItemType,
        val date: String?,
    ) : CalendarEvent
}
