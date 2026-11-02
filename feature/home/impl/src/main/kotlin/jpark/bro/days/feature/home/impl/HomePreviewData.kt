package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import java.time.LocalDate

/** product-spec 9장 솔로 샘플 (오늘 = 2026.11.05) */
@Suppress("MagicNumber") // 샘플 데이터
internal val PreviewToday: LocalDate = LocalDate.of(2026, 11, 5)

@Suppress("MagicNumber") // 샘플 데이터
private val PreviewItems: List<Item> =
    listOf(
        previewItem("discharge", previewDday("전역일", LocalDate.of(2027, 3, 19), ItemIcon.FLAG)),
        previewItem(
            "mom",
            previewDday("엄마 생신", LocalDate.of(1970, 1, 2), ItemIcon.CAKE).copy(repeatRule = RepeatRule.YEARLY),
        ),
        previewItem(
            "quit",
            previewDday(
                "금연",
                LocalDate.of(2026, 8, 7),
                ItemIcon.STAR,
            ).copy(countMode = CountMode.DPLUS, countFirstDayAsOne = true),
        ),
        previewItem("toeic", previewDday("토익 시험", LocalDate.of(2026, 11, 8), ItemIcon.SCHOOL)),
        previewItem(
            "dinner",
            ItemContent(
                type = ItemType.PLAN,
                title = "성수 저녁",
                colorKey = AppThemeType.PINK.name,
                iconKey = ItemIcon.EVENT.name,
                date = LocalDate.of(2026, 11, 6),
                timeSlot = TimeSlot.EVENING,
            ),
        ),
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

private fun previewItem(
    id: String,
    content: ItemContent,
) = Item(id = id, content = content, ownerUserId = "user-me", updatedBy = "user-me", createdAt = 0L, updatedAt = 0L)

internal fun previewState(pinnedItemId: String?) =
    HomeState(
        isLoading = false,
        hasItems = true,
        today = PreviewToday,
        content = buildHomeContent(PreviewItems, pinnedItemId, PreviewToday),
        showCoupleBanner = true,
    )
