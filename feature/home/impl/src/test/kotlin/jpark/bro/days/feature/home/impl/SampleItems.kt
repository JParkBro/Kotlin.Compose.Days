package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.model.Category
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

internal fun sampleItem(
    id: String,
    content: ItemContent,
) = Item(id = id, content = content, ownerUserId = "user-me", updatedBy = "user-me", createdAt = 0L, updatedAt = 0L)

private fun dday(
    title: String,
    date: LocalDate,
    countMode: CountMode = CountMode.DDAY,
    countFirstDayAsOne: Boolean = false,
    repeatRule: RepeatRule = RepeatRule.NONE,
) = ItemContent(
    type = ItemType.DDAY,
    title = title,
    colorKey = "PINK",
    iconKey = "STAR",
    category = Category.ETC,
    date = date,
    countMode = countMode,
    countFirstDayAsOne = countFirstDayAsOne,
    repeatRule = repeatRule,
)

/** 전역일 (대표) D-134 */
internal val Discharge = sampleItem("discharge", dday("전역일", LocalDate.of(2027, 3, 19)))

/** 엄마 생신 (매년) D-58 */
internal val MomBirthday = sampleItem("mom", dday("엄마 생신", LocalDate.of(1970, 1, 2), repeatRule = RepeatRule.YEARLY))

/** 금연 (D+, 1일 세기) D+91 */
internal val QuitSmoking =
    sampleItem("quit", dday("금연", LocalDate.of(2026, 8, 7), CountMode.DPLUS, countFirstDayAsOne = true))

/** 토익 D-3 */
internal val Toeic = sampleItem("toeic", dday("토익 시험", LocalDate.of(2026, 11, 8)))

private fun plan(
    title: String,
    date: LocalDate?,
    time: LocalTime? = null,
    showAsDday: Boolean = false,
    isDone: Boolean = false,
) = ItemContent(
    type = ItemType.PLAN,
    title = title,
    colorKey = "PINK",
    iconKey = "EVENT",
    date = date,
    timeSlot = if (time != null) TimeSlot.EXACT else TimeSlot.ALL_DAY,
    time = time,
    showAsDday = showAsDday,
    isDone = isDone,
)

/** 성수 저녁 · 내일 저녁 7시 */
internal val SeongsuDinner = sampleItem("dinner", plan("성수 저녁", LocalDate.of(2026, 11, 6), LocalTime.of(19, 0)))

/** 제주 여행 (디데이로 세기) D-12 */
internal val JejuTrip = sampleItem("jeju", plan("제주 여행", LocalDate.of(2026, 11, 17), showAsDday = true))

/** 날짜 없는 일정 (하고 싶은 것) */
internal val Someday = sampleItem("someday", plan("캠핑 가기", null))
