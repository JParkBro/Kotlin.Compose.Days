package jpark.bro.days.feature.home.impl

import jpark.bro.days.core.domain.dday.ddayOn
import jpark.bro.days.core.domain.dday.sortTime
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemType
import java.time.LocalDate

/** D-day를 세는 항목 한 줄 */
internal data class HomeRow(
    val item: Item,
    val dday: DdayCount,
)

/** 맨 위 히어로. [isPinned]가 false면 고정이 없어 가장 가까운 디데이를 대신 보여주는 것 */
internal data class HomeHero(
    val row: HomeRow,
    val isPinned: Boolean,
)

/** 솔로 홈 (product-spec 4-1): 히어로 → 다가오는 일정 → 디데이 리스트 */
internal data class HomeContent(
    val hero: HomeHero? = null,
    val nextPlan: Item? = null,
    val rows: List<HomeRow> = emptyList(),
)

/**
 * - 리스트: 디데이 + "디데이로 세기" 켠 일정. 다가오는 것(가까운 순) → 지난 것(최근 순). 히어로 항목은 뺀다
 * - 히어로: 대표로 고정한 디데이, 없으면 가장 가까운 다가오는 항목, 그것도 없으면 없음
 * - 다가오는 일정: 오늘 이후 미완료 일정 중 가장 빠른 것 (날짜 → 시각 순, [sortTime])
 */
internal fun buildHomeContent(
    items: List<Item>,
    pinnedItemId: String?,
    today: LocalDate,
): HomeContent {
    val rows =
        items
            .filter { it.content.type == ItemType.DDAY || it.content.showAsDday }
            .mapNotNull { item -> item.content.ddayOn(today)?.let { HomeRow(item, it) } }
            .sortedWith(compareBy({ it.dday.orderGroup }, { it.dday.distance }))

    val pinned = rows.firstOrNull { it.item.id == pinnedItemId && it.item.content.type == ItemType.DDAY }
    val hero =
        pinned?.let { HomeHero(it, isPinned = true) }
            ?: rows.firstOrNull { it.dday !is DdayCount.Elapsed }?.let { HomeHero(it, isPinned = false) }

    val nextPlan =
        items
            .filter { it.content.type == ItemType.PLAN && !it.content.isDone }
            .filter { item -> item.content.date?.let { !it.isBefore(today) } ?: false }
            .minWithOrNull(compareBy<Item>({ it.content.date }).thenBy { it.content.sortTime })

    return HomeContent(
        hero = hero,
        nextPlan = nextPlan,
        rows = rows.filter { it.item.id != hero?.row?.item?.id },
    )
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
