package jpark.bro.days.core.domain.anniversary

import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import java.time.LocalDate

/** 다가오는 기념일 수 (product-spec 4-4) */
private const val UPCOMING_COUNT = 3

/** 100일 단위가 3번 들어가는 기간이면 다가오는 3개를 항상 찾는다 */
private const val LOOKAHEAD_DAYS = 300L

/**
 * 디데이 상세의 "다가오는 기념일" 타임라인 (product-spec 4-4).
 * 반복이 없고 기준일이 오늘이거나 지난(D+로 세는) 디데이만: 기준일부터 100일 단위 · 주년.
 * 바로 직전에 지난 1개 + 오늘 포함 다가오는 3개를 날짜순으로 돌려준다. 해당 없으면 빈 목록.
 * 1일 세기는 D+ 방식에서만 적용한다 (product-spec 2-2).
 */
fun itemMilestones(
    content: ItemContent,
    today: LocalDate,
): List<AutoAnniversary> {
    val start = content.date
    val counting = content.type == ItemType.DDAY && content.repeatRule == RepeatRule.NONE
    if (start == null || !counting || start.isAfter(today)) return emptyList()

    val all =
        autoAnniversaries(
            startDate = start,
            countFirstDayAsOne = content.countMode == CountMode.DPLUS && content.countFirstDayAsOne,
            birthdays = emptyList(),
            period = start..today.plusDays(LOOKAHEAD_DAYS),
        )
    val passed = all.lastOrNull { it.date.isBefore(today) }
    val upcoming = all.filter { !it.date.isBefore(today) }.take(UPCOMING_COUNT)
    return listOfNotNull(passed) + upcoming
}
