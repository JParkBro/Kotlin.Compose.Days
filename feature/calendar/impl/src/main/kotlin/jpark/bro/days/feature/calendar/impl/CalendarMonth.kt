package jpark.bro.days.feature.calendar.impl

import jpark.bro.days.core.domain.dday.ddayOn
import jpark.bro.days.core.domain.dday.occurrencesIn
import jpark.bro.days.core.domain.dday.sortTime
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private const val DAYS_IN_WEEK = 7

/** 그리드 한 칸. [inMonth]가 false면 이전 · 다음 달 날짜 (흐리게, 누를 수 없음) */
internal data class CalendarDay(
    val date: LocalDate,
    val inMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    /** 디데이 · 기념일: primary 점 */
    val hasDday: Boolean,
    /** 일정: accent 점 */
    val hasPlan: Boolean,
)

/** 리스트 한 줄. [date]는 캘린더에 놓인 날(반복 항목은 그 달의 도래일), [dday]는 디데이만 */
internal data class CalendarEntry(
    val item: Item,
    val date: LocalDate,
    val dday: DdayCount?,
) {
    val key: String get() = "${item.id}_$date"
}

internal data class CalendarMonth(
    val weeks: List<List<CalendarDay>> = emptyList(),
    val entries: List<CalendarEntry> = emptyList(),
)

/**
 * 월 그리드 + 리스트 (product-spec 4-3, theme-guide 5.7).
 * - 그리드: 일요일 시작, 필요한 주 수만큼(5~6주), 앞뒤 달 날짜 포함
 * - 리스트: 날짜를 골랐으면 그날, 아니면 이번 달 전체. 날짜 → 시각([sortTime]) → 제목 순
 */
internal fun buildCalendarMonth(
    items: List<Item>,
    month: YearMonth,
    selectedDate: LocalDate?,
    today: LocalDate,
): CalendarMonth {
    val monthEntries =
        items
            .flatMap { item ->
                item.content.occurrencesIn(month).map { date ->
                    CalendarEntry(item, date, item.content.ddayOn(today).takeIf { item.content.type == ItemType.DDAY })
                }
            }.sortedWith(
                compareBy<CalendarEntry> { it.date }
                    .thenBy { it.item.content.sortTime }
                    .thenBy { it.item.content.title },
            )
    val byDate = monthEntries.groupBy { it.date }

    val first = month.atDay(1)
    // 일요일 시작: 일=0, 월=1 … 토=6
    val start = first.minusDays((first.dayOfWeek.value % DAYS_IN_WEEK).toLong())
    val last = month.atEndOfMonth()
    val end = last.plusDays(((DayOfWeek.SATURDAY.value - last.dayOfWeek.value + DAYS_IN_WEEK) % DAYS_IN_WEEK).toLong())
    val days =
        generateSequence(start) { it.plusDays(1) }
            .takeWhile { !it.isAfter(end) }
            .map { date ->
                val inMonth = YearMonth.from(date) == month
                val dayEntries = if (inMonth) byDate[date].orEmpty() else emptyList()
                CalendarDay(
                    date = date,
                    inMonth = inMonth,
                    isToday = date == today,
                    isSelected = date == selectedDate,
                    hasDday = dayEntries.any { it.item.content.type == ItemType.DDAY },
                    hasPlan = dayEntries.any { it.item.content.type == ItemType.PLAN },
                )
            }.toList()

    return CalendarMonth(
        weeks = days.chunked(DAYS_IN_WEEK),
        entries = if (selectedDate != null) byDate[selectedDate].orEmpty() else monthEntries,
    )
}
