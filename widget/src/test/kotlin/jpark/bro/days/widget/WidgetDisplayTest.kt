package jpark.bro.days.widget

import jpark.bro.days.core.domain.dday.ItemDday
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.WidgetConfig
import jpark.bro.days.core.model.WidgetSize
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** product-spec 9장 시나리오 (오늘 = 2026.11.05) */
class WidgetDisplayTest {
    private val today = LocalDate.of(2026, 11, 5)

    private fun dday(
        id: String,
        title: String,
        date: LocalDate,
        countMode: CountMode = CountMode.DDAY,
    ) = Item(
        id = id,
        content =
            ItemContent(
                type = ItemType.DDAY,
                title = title,
                colorKey = "PINK",
                iconKey = "STAR",
                date = date,
                countMode = countMode,
                countFirstDayAsOne = countMode == CountMode.DPLUS,
            ),
        ownerUserId = "user-me",
        updatedBy = "user-me",
        createdAt = 0L,
        updatedAt = 0L,
    )

    private val discharge = dday("discharge", "전역일", LocalDate.of(2027, 3, 19))
    private val toeic = dday("toeic", "토익 시험", LocalDate.of(2026, 11, 8))
    private val quit = dday("quit", "금연", LocalDate.of(2026, 8, 7), CountMode.DPLUS)
    private val items = listOf(discharge, toeic, quit)

    private fun config(itemId: String?) = WidgetConfig(widgetId = 1, size = WidgetSize.SMALL, itemId = itemId)

    @Test
    fun `설정에서 고른 디데이를 보여줌`() {
        val display = buildWidgetDisplay(items, pinnedItemId = discharge.id, config = config(quit.id), today = today)

        assertEquals(WidgetDisplay.Dday(ItemDday(quit, DdayCount.Elapsed(91))), display)
    }

    @Test
    fun `고른 디데이가 없으면 대표 고정 디데이`() {
        val display = buildWidgetDisplay(items, pinnedItemId = discharge.id, config = null, today = today)

        assertEquals(discharge, (display as WidgetDisplay.Dday).entry.item)
    }

    @Test
    fun `고정도 없으면 가장 가까운 다가오는 디데이`() {
        val display = buildWidgetDisplay(items, pinnedItemId = null, config = config(null), today = today)

        assertEquals(toeic, (display as WidgetDisplay.Dday).entry.item)
    }

    @Test
    fun `고른 디데이가 삭제됐으면 다시 고르라는 안내`() {
        assertEquals(WidgetDisplay.Missing, buildWidgetDisplay(items, null, config("deleted"), today))
    }

    @Test
    fun `보여줄 디데이가 하나도 없으면 빈 안내`() {
        assertEquals(WidgetDisplay.Empty, buildWidgetDisplay(emptyList(), null, null, today))
        assertEquals(WidgetDisplay.Empty, buildWidgetDisplay(listOf(quit), null, null, today))
    }
}
