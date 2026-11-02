package jpark.bro.days.feature.calendar.impl

import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import java.time.LocalDate
import java.time.LocalTime

/** product-spec 9장 시나리오 (오늘 = 2026.11.05) */
internal val Today: LocalDate = LocalDate.of(2026, 11, 5)

private fun item(
    id: String,
    content: ItemContent,
) = Item(id = id, content = content, ownerUserId = "user-me", updatedBy = "user-me", createdAt = 0L, updatedAt = 0L)

private fun dday(
    title: String,
    date: LocalDate,
    repeatRule: RepeatRule = RepeatRule.NONE,
) = ItemContent(
    type = ItemType.DDAY,
    title = title,
    colorKey = "PINK",
    iconKey = "STAR",
    date = date,
    countMode = CountMode.DDAY,
    repeatRule = repeatRule,
)

private fun plan(
    title: String,
    date: LocalDate?,
    time: LocalTime? = null,
    isDone: Boolean = false,
) = ItemContent(
    type = ItemType.PLAN,
    title = title,
    colorKey = "PINK",
    iconKey = "EVENT",
    date = date,
    timeSlot = if (time != null) TimeSlot.EXACT else TimeSlot.EVENING,
    time = time,
    place = "성수동",
    isDone = isDone,
)

/** 토익 11.08 D-3 */
internal val Toeic = item("toeic", dday("토익 시험", LocalDate.of(2026, 11, 8)))

/** 매월 17일 (용돈 날) */
internal val Allowance = item("allowance", dday("용돈 날", LocalDate.of(2026, 1, 17), RepeatRule.MONTHLY))

/** 엄마 생신 매년 1.2 (11월에는 없음) */
internal val MomBirthday = item("mom", dday("엄마 생신", LocalDate.of(1970, 1, 2), RepeatRule.YEARLY))

/** 성수 저녁 11.06 */
internal val SeongsuDinner = item("dinner", plan("성수 저녁", LocalDate.of(2026, 11, 6)))

/** 같은 날 점심 12시 (시간 지정) */
internal val Lunch = item("lunch", plan("점심 약속", LocalDate.of(2026, 11, 6), LocalTime.of(12, 0)))

/** 날짜 없는 일정 */
internal val Someday = item("someday", plan("캠핑 가기", null))
