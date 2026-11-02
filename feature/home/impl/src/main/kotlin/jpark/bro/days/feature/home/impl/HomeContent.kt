package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.domain.dday.FeaturedItem
import jpark.bro.days.core.domain.dday.ItemDday
import jpark.bro.days.core.domain.dday.countingItems
import jpark.bro.days.core.domain.dday.featured
import jpark.bro.days.core.domain.dday.sortTime
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemType
import java.time.LocalDate

/** 솔로 홈 (product-spec 4-1): 히어로 → 다가오는 일정 → 디데이 리스트 */
internal data class HomeContent(
    val hero: FeaturedItem? = null,
    val nextPlan: Item? = null,
    val rows: List<ItemDday> = emptyList(),
)

/**
 * - 리스트: [countingItems]에서 히어로 항목을 뺀 것
 * - 히어로: [featured] (대표 고정 → 가장 가까운 다가오는 항목)
 * - 다가오는 일정: 오늘 이후 미완료 일정 중 가장 빠른 것 (날짜 → 시각 순, [sortTime])
 */
internal fun buildHomeContent(
    items: List<Item>,
    pinnedItemId: String?,
    today: LocalDate,
): HomeContent {
    val rows = countingItems(items, today)
    val hero = rows.featured(pinnedItemId)

    val nextPlan =
        items
            .filter { it.content.type == ItemType.PLAN && !it.content.isDone }
            .filter { item -> item.content.date?.let { !it.isBefore(today) } ?: false }
            .minWithOrNull(compareBy<Item>({ it.content.date }).thenBy { it.content.sortTime })

    return HomeContent(
        hero = hero,
        nextPlan = nextPlan,
        rows = rows.filter { it.item.id != hero?.entry?.item?.id },
    )
}
