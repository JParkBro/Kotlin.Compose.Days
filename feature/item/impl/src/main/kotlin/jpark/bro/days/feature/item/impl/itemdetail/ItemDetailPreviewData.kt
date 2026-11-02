package jpark.bro.days.feature.item.impl.itemdetail

import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.Category
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.TimeSlot
import java.time.LocalDate

/** product-spec 9장 샘플 (오늘 = 2026.11.05) */
@Suppress("MagicNumber") // 샘플 데이터
private val PreviewToday: LocalDate = LocalDate.of(2026, 11, 5)

private fun previewState(content: ItemContent): ItemDetailState =
    Item("preview", content, "user-me", "user-me", 0L, 0L).toDetailState(PreviewToday)

/** 금연 D+91 (1일 세기, 메모, 타임라인) */
@Suppress("MagicNumber") // 샘플 데이터
internal val PreviewQuitSmoking: ItemDetailState =
    previewState(
        ItemContent(
            type = ItemType.DDAY,
            title = "금연",
            colorKey = AppThemeType.SAGE.name,
            iconKey = ItemIcon.FLAG.name,
            category = Category.GOAL,
            date = LocalDate.of(2026, 8, 7),
            countMode = CountMode.DPLUS,
            countFirstDayAsOne = true,
            memo = "담배 대신 산책하기. 100일 되면 운동화 사기!",
        ),
    )

/** 전역일 D-134 (카운트다운이라 타임라인 없음) */
@Suppress("MagicNumber") // 샘플 데이터
internal val PreviewDischarge: ItemDetailState =
    previewState(
        ItemContent(
            type = ItemType.DDAY,
            title = "전역일",
            colorKey = AppThemeType.NAVY.name,
            iconKey = ItemIcon.FLAG.name,
            category = Category.GOAL,
            date = LocalDate.of(2027, 3, 19),
            countMode = CountMode.DDAY,
        ),
    )

/** 일정: 성수 저녁 */
@Suppress("MagicNumber") // 샘플 데이터
internal val PreviewDinner: ItemDetailState =
    previewState(
        ItemContent(
            type = ItemType.PLAN,
            title = "성수 저녁",
            colorKey = AppThemeType.PINK.name,
            iconKey = ItemIcon.EVENT.name,
            date = LocalDate.of(2026, 11, 6),
            timeSlot = TimeSlot.EVENING,
            place = "성수동",
        ),
    )
