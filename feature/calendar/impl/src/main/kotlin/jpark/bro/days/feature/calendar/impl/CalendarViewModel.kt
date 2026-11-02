package jpark.bro.days.feature.calendar.impl

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth

internal class CalendarViewModel(
    private val itemRepository: ItemRepository,
    private val clock: Clock,
) : MviViewModel<CalendarState, CalendarAction, CalendarEvent>(initialState(LocalDate.now(clock))) {
    private var items: List<Item> = emptyList()

    init {
        itemRepository
            .observeItems()
            .onEach { latest ->
                items = latest
                update { copy(isLoading = false, today = LocalDate.now(clock)) }
            }.launchIn(viewModelScope)
    }

    override fun onAction(action: CalendarAction) {
        when (action) {
            CalendarAction.PreviousMonthClicked -> {
                update { copy(month = month.minusMonths(1), selectedDate = null) }
            }

            CalendarAction.NextMonthClicked -> {
                update { copy(month = month.plusMonths(1), selectedDate = null) }
            }

            CalendarAction.TodayClicked -> {
                update { copy(month = YearMonth.from(today), selectedDate = today) }
            }

            is CalendarAction.DateClicked -> {
                update { copy(selectedDate = action.date.takeIf { it != selectedDate }) }
            }

            is CalendarAction.ItemClicked -> {
                openItem(action)
            }

            is CalendarAction.DoneToggled -> {
                toggleDone(action.id)
            }

            CalendarAction.AddButtonClicked -> {
                setState { copy(isAddSheetOpen = true) }
            }

            CalendarAction.AddSheetDismissed -> {
                setState { copy(isAddSheetOpen = false) }
            }

            is CalendarAction.AddClicked -> {
                setState { copy(isAddSheetOpen = false) }
                sendEvent(CalendarEvent.OpenItemEdit(action.type, date = currentState.selectedDate))
            }
        }
    }

    /** 월 · 선택 · 항목이 바뀌면 그리드와 리스트를 다시 만든다 */
    private fun update(reduce: CalendarState.() -> CalendarState) {
        setState {
            val next = reduce()
            next.copy(calendar = buildCalendarMonth(items, next.month, next.selectedDate, next.today))
        }
    }

    private fun openItem(action: CalendarAction.ItemClicked) {
        sendEvent(
            when (action.type) {
                ItemType.DDAY -> CalendarEvent.OpenItemDetail(action.id)
                ItemType.PLAN -> CalendarEvent.OpenItemEdit(ItemType.PLAN, itemId = action.id)
            },
        )
    }

    private fun toggleDone(id: String) {
        val item = items.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            itemRepository
                .saveItem(id, item.content.copy(isDone = !item.content.isDone))
                .onFailure { sendEvent(CalendarEvent.DoneToggleFailed) }
        }
    }
}

private fun initialState(today: LocalDate) = CalendarState(today = today, month = YearMonth.from(today))
