package jpark.bro.days.feature.calendar.impl

import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

/** product-spec 9장 샘플 (오늘 = 2026.11.05) */
@Suppress("MagicNumber") // 샘플 데이터
private val PreviewToday: LocalDate = LocalDate.of(2026, 11, 5)

internal val PreviewTomorrow: LocalDate = PreviewToday.plusDays(1)

@Suppress("MagicNumber") // 샘플 데이터
private val PreviewItems: List<Item> =
    listOf(
        previewItem("toeic", previewDday("토익 시험", LocalDate.of(2026, 11, 8), ItemIcon.SCHOOL)),
        previewItem("jeju", previewDday("제주 여행", LocalDate.of(2026, 11, 17), ItemIcon.FLIGHT)),
        previewItem(
            "allowance",
            previewDday("용돈 날", LocalDate.of(2026, 1, 25), ItemIcon.GIFT).copy(repeatRule = RepeatRule.MONTHLY),
        ),
        previewItem("dinner", previewPlan("성수 저녁", PreviewTomorrow, TimeSlot.EVENING)),
        previewItem(
            "exhibition",
            previewPlan("전시회", LocalDate.of(2026, 11, 22), TimeSlot.EXACT).copy(time = LocalTime.of(14, 0)),
        ),
        previewItem("cleaning", previewPlan("대청소", LocalDate.of(2026, 11, 1), TimeSlot.ALL_DAY).copy(isDone = true)),
    )

private fun previewDday(
    title: String,
    date: LocalDate,
    icon: ItemIcon,
) = ItemContent(
    type = ItemType.DDAY,
    title = title,
    colorKey = AppThemeType.PINK.name,
    iconKey = icon.name,
    date = date,
    countMode = CountMode.DDAY,
)

private fun previewPlan(
    title: String,
    date: LocalDate,
    slot: TimeSlot,
) = ItemContent(
    type = ItemType.PLAN,
    title = title,
    colorKey = AppThemeType.PINK.name,
    iconKey = ItemIcon.EVENT.name,
    date = date,
    timeSlot = slot,
    place = "성수동",
)

private fun previewItem(
    id: String,
    content: ItemContent,
) = Item(id = id, content = content, ownerUserId = "user-me", updatedBy = "user-me", createdAt = 0L, updatedAt = 0L)

internal fun previewCalendarState(selectedDate: LocalDate?): CalendarState {
    val month = YearMonth.from(PreviewToday)
    return CalendarState(
        isLoading = false,
        today = PreviewToday,
        month = month,
        selectedDate = selectedDate,
        calendar = buildCalendarMonth(PreviewItems, month, selectedDate, PreviewToday),
    )
}
