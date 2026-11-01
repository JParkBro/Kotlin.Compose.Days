package jpark.bro.days.feature.calendar.impl

import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.calendar.api.CalendarKey
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey

internal class CalendarViewModel : MviViewModel<CalendarState, CalendarAction, CalendarEvent>(CalendarState()) {
    override fun onAction(action: CalendarAction) {
        when (action) {
            is CalendarAction.ItemClicked -> sendEvent(CalendarEvent.OpenItemDetail(action.id))
            is CalendarAction.AddClicked -> sendEvent(CalendarEvent.OpenItemEdit(action.type, action.date))
        }
    }
}
