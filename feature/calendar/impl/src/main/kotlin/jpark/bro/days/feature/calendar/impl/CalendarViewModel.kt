package jpark.bro.days.feature.calendar.impl

import jpark.bro.days.core.ui.MviViewModel

internal class CalendarViewModel : MviViewModel<CalendarState, CalendarAction, CalendarEvent>(CalendarState()) {
    override fun onAction(action: CalendarAction) {
        when (action) {
            is CalendarAction.ItemClicked -> sendEvent(CalendarEvent.OpenItemDetail(action.id))
            is CalendarAction.AddClicked -> sendEvent(CalendarEvent.OpenItemEdit(action.type, action.date))
        }
    }
}
