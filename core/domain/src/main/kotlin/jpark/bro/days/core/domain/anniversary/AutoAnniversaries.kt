package jpark.bro.days.core.domain.anniversary

import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.UserBirthday
import java.time.LocalDate

private const val DAY_UNIT = 100

/**
 * period(양끝 포함) 사이의 자동 기념일을 날짜순으로 계산한다 (product-spec 2-3).
 * - 100일 단위: 1일 세기를 켜면 사귄 날이 1일이므로 N일 = 사귄 날 + (N - 1)
 * - 주년 · 생일: 같은 월 · 일. 2월 29일은 평년에 2월 28일
 * - hiddenRuleKeys에 있는 기념일은 뺀다
 */
fun autoAnniversaries(
    startDate: LocalDate,
    countFirstDayAsOne: Boolean,
    birthdays: List<UserBirthday>,
    period: ClosedRange<LocalDate>,
    hiddenRuleKeys: Set<String> = emptySet(),
): List<AutoAnniversary> {
    val from = period.start
    val to = period.endInclusive
    val dayOffset = if (countFirstDayAsOne) 1L else 0L

    val days =
        generateSequence(DAY_UNIT) { it + DAY_UNIT }
            .map { n -> AutoAnniversary.Days(n, startDate.plusDays(n - dayOffset)) }
            .takeWhile { !it.date.isAfter(to) }

    val years =
        generateSequence(1) { it + 1 }
            .map { n -> AutoAnniversary.Years(n, startDate.plusYears(n.toLong())) }
            .takeWhile { !it.date.isAfter(to) }

    val birthdayAnniversaries =
        birthdays.flatMap { birthday ->
            (maxOf(from.year, birthday.birthDate.year + 1)..to.year).map { year ->
                AutoAnniversary.Birthday(birthday.userId, year, birthday.birthDate.withYear(year))
            }
        }

    return (days + years + birthdayAnniversaries)
        .filter { it.date in period }
        .filterNot { it.ruleKey in hiddenRuleKeys }
        .sortedWith(compareBy({ it.date }, { it.ruleKey }))
        .toList()
}
