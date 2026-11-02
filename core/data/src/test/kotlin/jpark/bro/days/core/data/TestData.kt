package jpark.bro.days.core.data

import jpark.bro.days.core.model.Category
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** product-spec 9장 시나리오 (오늘 = 2026.11.05) */
internal val Seoul: ZoneId = ZoneId.of("Asia/Seoul")
internal val Today: Instant = LocalDate.of(2026, 11, 5).atStartOfDay(Seoul).toInstant()
internal const val ME = "user-me"

/** 솔로: 전역일 (대표) 2027.03.19 */
internal val DischargeDday =
    ItemContent(
        type = ItemType.DDAY,
        title = "전역일",
        colorKey = "primary",
        iconKey = "star",
        category = Category.GOAL,
        date = LocalDate.of(2027, 3, 19),
        countMode = CountMode.DDAY,
    )

/** 솔로: 엄마 생신 (매년) 2027.01.02 */
internal val MomBirthday =
    DischargeDday.copy(
        title = "엄마 생신",
        category = Category.BIRTHDAY,
        date = LocalDate.of(2027, 1, 2),
        repeatRule = RepeatRule.YEARLY,
    )

/** 일정: 성수 저녁 · 저녁 7시 */
internal val DinnerPlan =
    ItemContent(
        type = ItemType.PLAN,
        title = "성수 저녁",
        colorKey = "accent",
        iconKey = "favorite",
        category = Category.DAILY,
        date = LocalDate.of(2026, 11, 6),
        timeSlot = TimeSlot.EXACT,
        time = LocalTime.of(19, 0),
        place = "성수",
    )
