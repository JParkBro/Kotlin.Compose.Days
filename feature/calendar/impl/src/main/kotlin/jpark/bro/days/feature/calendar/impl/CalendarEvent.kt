package jpark.bro.days.feature.calendar.impl

import jpark.bro.days.core.model.ItemType
import java.time.LocalDate

internal sealed interface CalendarEvent {
    data class OpenItemDetail(
        val id: String,
    ) : CalendarEvent

    /** [itemId]가 있으면 수정, 없으면 추가. [date]는 추가할 때 미리 채울 날짜 */
    data class OpenItemEdit(
        val type: ItemType,
        val itemId: String? = null,
        val date: LocalDate? = null,
    ) : CalendarEvent

    data object DoneToggleFailed : CalendarEvent
}
