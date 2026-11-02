package jpark.bro.days.widget

import jpark.bro.days.core.domain.dday.ItemDday
import jpark.bro.days.core.domain.dday.countingItems
import jpark.bro.days.core.domain.dday.ddayOn
import jpark.bro.days.core.domain.dday.featured
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.WidgetConfig
import java.time.LocalDate

/** 위젯 한 개에 보일 내용 */
internal sealed interface WidgetDisplay {
    /** 디데이 하나 (누르면 상세) */
    data class Dday(
        val entry: ItemDday,
    ) : WidgetDisplay

    /** 보여줄 디데이가 하나도 없음 (누르면 앱) */
    data object Empty : WidgetDisplay

    /** 설정한 디데이가 삭제됨 (누르면 앱, 위젯 설정에서 다시 고르도록) */
    data object Missing : WidgetDisplay
}

/**
 * 설정에서 고른 디데이를 보여주고, 고른 것이 없으면 홈 히어로와 같은 규칙([featured])으로 고른다 (product-spec 8장).
 * 고른 디데이가 삭제됐거나 날짜가 없으면 [WidgetDisplay.Missing]
 */
internal fun buildWidgetDisplay(
    items: List<Item>,
    pinnedItemId: String?,
    config: WidgetConfig?,
    today: LocalDate,
): WidgetDisplay {
    val chosenId = config?.itemId
    if (chosenId != null) {
        val item = items.firstOrNull { it.id == chosenId }
        val dday = item?.content?.ddayOn(today)
        return if (item != null && dday != null) WidgetDisplay.Dday(ItemDday(item, dday)) else WidgetDisplay.Missing
    }
    return countingItems(items, today).featured(pinnedItemId)?.let { WidgetDisplay.Dday(it.entry) }
        ?: WidgetDisplay.Empty
}
