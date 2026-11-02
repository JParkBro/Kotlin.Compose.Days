package jpark.bro.days.core.domain.dday

import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * [month] 안에서 항목이 캘린더에 놓이는 날 (product-spec 4-3).
 * - 일정 · 반복 없는 디데이: 그 날짜 하나
 * - 매년 · 매월: 기준일 이후의 도래일. [nextOccurrence]와 같이 매번 기준일에서 더하므로
 *   2월 29일 · 31일처럼 없는 날은 그 달 말일이 된다
 * - 날짜가 없으면(하고 싶은 것) 없음
 */
fun ItemContent.occurrencesIn(month: YearMonth): List<LocalDate> {
    val base = date ?: return emptyList()
    val repeat = if (type == ItemType.PLAN) RepeatRule.NONE else repeatRule
    val candidate =
        when (repeat) {
            RepeatRule.NONE -> base
            RepeatRule.YEARLY -> base.plusYears((month.year - base.year).toLong())
            RepeatRule.MONTHLY -> base.plusMonths(ChronoUnit.MONTHS.between(YearMonth.from(base), month))
        }
    return listOf(candidate).filter { YearMonth.from(it) == month && !it.isBefore(base) }
}
