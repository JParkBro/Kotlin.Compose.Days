package jpark.bro.days.core.domain.anniversary

import jpark.bro.days.core.domain.dday.calculateDday
import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.UserBirthday
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** 홈 "다음 기념일" 카드 (product-spec 4-1) */
data class NextAnniversary(
    val anniversary: AutoAnniversary,
    /** D-N, 당일이면 D-Day */
    val dday: DdayCount,
    /** 직전 기념일(없으면 사귄 날)부터 다음 기념일까지 지난 비율 0..1 */
    val progress: Float,
)

/** 다음 기념일을 찾는 범위. 주년 · 100일 단위를 모두 숨겨도 이 안에서 찾지 못하면 카드를 숨긴다 */
private const val SEARCH_YEARS = 2L

/**
 * 오늘 포함 가장 가까운 자동 기념일과 진행 비율 (숨긴 것은 뺀다).
 * 진행 비율은 직전 기념일(없으면 사귄 날)에서 다음 기념일까지 중 오늘까지 지난 날의 비율이다.
 */
fun nextAnniversary(
    startDate: LocalDate,
    countFirstDayAsOne: Boolean,
    birthdays: List<UserBirthday>,
    today: LocalDate,
    hiddenRuleKeys: Set<String> = emptySet(),
): NextAnniversary? {
    val all =
        autoAnniversaries(
            startDate = startDate,
            countFirstDayAsOne = countFirstDayAsOne,
            birthdays = birthdays,
            period = startDate..maxOf(startDate, today).plusYears(SEARCH_YEARS),
            hiddenRuleKeys = hiddenRuleKeys,
        )
    val next = all.firstOrNull { !it.date.isBefore(today) } ?: return null
    val previous = all.lastOrNull { it.date.isBefore(today) }?.date ?: startDate
    val total = ChronoUnit.DAYS.between(previous, next.date)
    val passed = ChronoUnit.DAYS.between(previous, today)
    val progress = if (total <= 0) 1f else (passed.toFloat() / total).coerceIn(0f, 1f)
    val dday = calculateDday(next.date, CountMode.DDAY, countFirstDayAsOne = false, RepeatRule.NONE, today)
    return NextAnniversary(next, dday, progress)
}
