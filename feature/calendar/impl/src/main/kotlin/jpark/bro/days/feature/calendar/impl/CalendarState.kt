package jpark.bro.days.feature.calendar.impl

import java.time.LocalDate
import java.time.YearMonth

internal data class CalendarState(
    val isLoading: Boolean = true,
    val today: LocalDate = LocalDate.MIN,
    val month: YearMonth = YearMonth.from(LocalDate.MIN),
    /** null이면 이번 달 전체를 리스트에 보여준다 (product-spec 4-3) */
    val selectedDate: LocalDate? = null,
    val calendar: CalendarMonth = CalendarMonth(),
    /** + 버튼 · "이 날 추가"로 연 [디데이 / 일정] 선택 시트 */
    val isAddSheetOpen: Boolean = false,
) {
    /** 오늘이 보이지 않을 때만 "오늘" 칩을 보여준다 */
    val showTodayChip: Boolean get() = month != YearMonth.from(today) || (selectedDate != null && selectedDate != today)
}
