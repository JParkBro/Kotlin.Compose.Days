package jpark.bro.days.core.domain.dday

import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.RepeatRule
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * start부터 today까지 지난 날 수.
 * countFirstDay가 켜져 있으면 start 당일을 1일로 센다 (한국식 커플 디데이).
 */
fun daysSince(
    start: LocalDate,
    today: LocalDate,
    countFirstDay: Boolean,
): Int = ChronoUnit.DAYS.between(start, today).toInt() + if (countFirstDay) 1 else 0

/**
 * 반복 항목의 다음 도래일 (today 포함). 반복이 없으면 date 그대로.
 * 매번 기준일에서 더하므로 2월 29일 · 31일처럼 없는 날은 그 달 말일이 되고, 다음 주기에는 원래 날짜로 돌아간다.
 */
fun nextOccurrence(
    date: LocalDate,
    repeat: RepeatRule,
    today: LocalDate,
): LocalDate =
    when (repeat) {
        RepeatRule.NONE -> date
        RepeatRule.YEARLY -> firstOnOrAfter(today, ChronoUnit.YEARS.between(date, today)) { date.plusYears(it) }
        RepeatRule.MONTHLY -> firstOnOrAfter(today, ChronoUnit.MONTHS.between(date, today)) { date.plusMonths(it) }
    }

/**
 * 항목의 디데이 값 (product-spec 2-2).
 * - 반복 항목: 다음 도래일까지 카운트다운
 * - D-Day 방식: 기준일이 지나면 D+로 계속 셈
 * - D+ 방식: 기준일 전에는 D-N, 당일부터 D+ (1일 세기를 켜면 당일이 D+1)
 */
fun calculateDday(
    date: LocalDate,
    countMode: CountMode,
    countFirstDayAsOne: Boolean,
    repeat: RepeatRule,
    today: LocalDate,
): DdayCount {
    if (repeat != RepeatRule.NONE) return countdown(nextOccurrence(date, repeat, today), today)
    return when (countMode) {
        CountMode.DDAY -> {
            countdown(date, today)
        }

        CountMode.DPLUS -> {
            when {
                date.isAfter(today) -> countdown(date, today)
                countFirstDayAsOne -> DdayCount.Elapsed(daysSince(date, today, countFirstDay = true))
                else -> countdown(date, today)
            }
        }
    }
}

private fun countdown(
    target: LocalDate,
    today: LocalDate,
): DdayCount {
    val days = ChronoUnit.DAYS.between(today, target).toInt()
    return when {
        days > 0 -> DdayCount.Remaining(days)
        days == 0 -> DdayCount.Today
        else -> DdayCount.Elapsed(-days)
    }
}

private inline fun firstOnOrAfter(
    today: LocalDate,
    startStep: Long,
    dateAt: (Long) -> LocalDate,
): LocalDate {
    var step = maxOf(0L, startStep)
    while (dateAt(step).isBefore(today)) step++
    return dateAt(step)
}
