package jpark.bro.days.widget.config

import jpark.bro.days.core.domain.dday.ItemDday
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.WidgetSize
import jpark.bro.days.widget.WidgetDisplay
import java.time.LocalDate

/** product-spec 9장 솔로 샘플 */
@Suppress("MagicNumber") // 샘플 데이터
private fun previewEntry(
    id: String,
    title: String,
    date: LocalDate,
    icon: ItemIcon,
    dday: DdayCount,
) = ItemDday(
    item =
        Item(
            id = id,
            content =
                ItemContent(
                    type = ItemType.DDAY,
                    title = title,
                    colorKey = AppThemeType.PINK.name,
                    iconKey = icon.name,
                    date = date,
                    countMode = CountMode.DDAY,
                ),
            ownerUserId = "user-me",
            updatedBy = "user-me",
            createdAt = 0L,
            updatedAt = 0L,
        ),
    dday = dday,
)

@Suppress("MagicNumber") // 샘플 데이터
private val PreviewChoices: List<ItemDday> =
    listOf(
        previewEntry("toeic", "토익 시험", LocalDate.of(2026, 11, 8), ItemIcon.SCHOOL, DdayCount.Remaining(3)),
        previewEntry("discharge", "전역일", LocalDate.of(2027, 3, 19), ItemIcon.FLAG, DdayCount.Remaining(134)),
        previewEntry("quit", "금연", LocalDate.of(2026, 8, 7), ItemIcon.STAR, DdayCount.Elapsed(91)),
    )

internal val PreviewWidgetConfigState: WidgetConfigState =
    WidgetConfigState(
        widgetId = 1,
        size = WidgetSize.SMALL,
        isLoading = false,
        choices = PreviewChoices,
        selectedItemId = "discharge",
        display = WidgetDisplay.Dday(PreviewChoices[1]),
    )
