package jpark.bro.days.core.domain.dday

import jpark.bro.days.core.domain.SampleScenario.TODAY
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ItemDdayTest {
    private fun content(
        type: ItemType,
        date: LocalDate?,
    ) = ItemContent(type = type, title = "t", colorKey = "PINK", iconKey = "STAR", date = date)

    @Test
    fun `날짜 없는 일정은 D-day가 없음`() {
        assertNull(content(ItemType.PLAN, null).ddayOn(TODAY))
    }

    @Test
    fun `일정은 반복 1일 세기를 무시하고 D-Day 방식으로 셈`() {
        // 성수 저녁 2026.11.06 → 내일
        val plan =
            content(ItemType.PLAN, LocalDate.of(2026, 11, 6))
                .copy(countMode = CountMode.DPLUS, countFirstDayAsOne = true, repeatRule = RepeatRule.YEARLY)

        assertEquals(DdayCount.Remaining(1), plan.ddayOn(TODAY))
    }

    @Test
    fun `디데이는 계산 방식과 1일 세기를 따름`() {
        // 금연 2026.08.07부터 1일 세기 → D+91
        val quit =
            content(
                ItemType.DDAY,
                LocalDate.of(2026, 8, 7),
            ).copy(countMode = CountMode.DPLUS, countFirstDayAsOne = true)

        assertEquals(DdayCount.Elapsed(91), quit.ddayOn(TODAY))
    }

    @Test
    fun `계산 방식이 비어 있는 디데이는 D-Day 방식으로 셈`() {
        assertEquals(DdayCount.Remaining(3), content(ItemType.DDAY, LocalDate.of(2026, 11, 8)).ddayOn(TODAY))
    }

    @Test
    fun `당일부터 7일 남은 날까지 임박`() {
        assertTrue(DdayCount.Today.isImminent())
        assertTrue(DdayCount.Remaining(1).isImminent())
        assertTrue(DdayCount.Remaining(7).isImminent())
    }

    @Test
    fun `8일 이상 남았거나 지난 날은 임박이 아님`() {
        assertFalse(DdayCount.Remaining(8).isImminent())
        assertFalse(DdayCount.Elapsed(1).isImminent())
    }
}
