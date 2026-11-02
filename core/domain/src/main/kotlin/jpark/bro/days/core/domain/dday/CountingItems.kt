package jpark.bro.days.core.domain.dday

import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemType
import java.time.LocalDate

/** D-day를 세는 항목 하나 */
data class ItemDday(
    val item: Item,
    val dday: DdayCount,
)

/** 홈 히어로 · 위젯 기본 표시. [isPinned]가 false면 고정이 없어 가장 가까운 항목을 대신 고른 것 */
data class FeaturedItem(
    val entry: ItemDday,
    val isPinned: Boolean,
)

/**
 * D-day를 세는 항목: 디데이 + "디데이로 세기" 켠 일정 (product-spec 4-1).
 * 다가오는 것(가까운 순) → 지난 것(최근 순)
 */
fun countingItems(
    items: List<Item>,
    today: LocalDate,
): List<ItemDday> =
    items
        .filter { it.content.type == ItemType.DDAY || it.content.showAsDday }
        .mapNotNull { item -> item.content.ddayOn(today)?.let { ItemDday(item, it) } }
        .sortedWith(compareBy({ it.dday.orderGroup }, { it.dday.distance }))

/**
 * 대표로 고정한 디데이, 없으면 가장 가까운 다가오는 항목, 그것도 없으면 null (홈 히어로 · 위젯 공용).
 * [this]는 [countingItems] 결과
 */
fun List<ItemDday>.featured(pinnedItemId: String?): FeaturedItem? {
    val pinned = firstOrNull { it.item.id == pinnedItemId && it.item.content.type == ItemType.DDAY }
    return pinned?.let { FeaturedItem(it, isPinned = true) }
        ?: firstOrNull { it.dday !is DdayCount.Elapsed }?.let { FeaturedItem(it, isPinned = false) }
}

/** 다가오는 것(0) → 지난 것(1) */
private val DdayCount.orderGroup: Int
    get() = if (this is DdayCount.Elapsed) 1 else 0

/** 같은 묶음 안에서 오늘과의 거리 (가까운 순) */
private val DdayCount.distance: Int
    get() =
        when (this) {
            DdayCount.Today -> 0
            is DdayCount.Remaining -> days
            is DdayCount.Elapsed -> days
        }
