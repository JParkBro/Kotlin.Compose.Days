package jpark.bro.days.core.domain.dday

import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import java.time.LocalDate

/** 임박 기준: 7일 이내 (product-spec 2-2) */
private const val IMMINENT_DAYS = 7

/**
 * 항목 하나의 D-day. 날짜가 없으면(하고 싶은 것) null.
 * 일정("디데이로 세기")은 반복 · 1일 세기 없이 D-Day 방식으로 센다.
 */
fun ItemContent.ddayOn(today: LocalDate): DdayCount? {
    val date = date ?: return null
    return when (type) {
        ItemType.DDAY -> {
            calculateDday(date, countMode ?: CountMode.DDAY, countFirstDayAsOne, repeatRule, today)
        }

        ItemType.PLAN -> {
            calculateDday(
                date,
                CountMode.DDAY,
                countFirstDayAsOne = false,
                repeat = RepeatRule.NONE,
                today = today,
            )
        }
    }
}

/** 당일 ~ 7일 이내 (위험 색 강조, product-spec 2-2) */
fun DdayCount.isImminent(): Boolean =
    when (this) {
        DdayCount.Today -> true
        is DdayCount.Remaining -> days <= IMMINENT_DAYS
        is DdayCount.Elapsed -> false
    }
