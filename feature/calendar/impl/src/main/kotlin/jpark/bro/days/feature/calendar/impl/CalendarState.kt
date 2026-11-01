package jpark.bro.days.feature.calendar.impl

import jpark.bro.days.core.model.ItemType
import jpark.bro.days.feature.calendar.api.CalendarKey
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey

internal data class CalendarState(
    val selectedDate: String? = null,
)
