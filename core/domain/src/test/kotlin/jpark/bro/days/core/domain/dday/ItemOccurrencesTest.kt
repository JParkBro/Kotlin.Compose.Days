package jpark.bro.days.core.domain.dday

import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class ItemOccurrencesTest {
    private fun content(
        date: LocalDate?,
        repeatRule: RepeatRule = RepeatRule.NONE,
        type: ItemType = ItemType.DDAY,
    ) = ItemContent(
        type = type,
        title = "t",
        colorKey = "PINK",
        iconKey = "STAR",
        date = date,
        countMode = CountMode.DDAY,
        repeatRule = repeatRule,
    )

    private val november = YearMonth.of(2026, 11)

    @Test
    fun `반복 없는 항목은 그 달에 있을 때만 그 날짜`() {
        assertEquals(listOf(LocalDate.of(2026, 11, 8)), content(LocalDate.of(2026, 11, 8)).occurrencesIn(november))
        assertTrue(content(LocalDate.of(2027, 3, 19)).occurrencesIn(november).isEmpty())
    }

    @Test
    fun `매년 반복은 같은 월 일 (샘플 - 엄마 생신 1월 2일)`() {
        val mom = content(LocalDate.of(1970, 1, 2), RepeatRule.YEARLY)

        assertEquals(listOf(LocalDate.of(2027, 1, 2)), mom.occurrencesIn(YearMonth.of(2027, 1)))
        assertTrue(mom.occurrencesIn(november).isEmpty())
    }

    @Test
    fun `매년 반복 2월 29일은 평년에 2월 28일`() {
        val leap = content(LocalDate.of(2024, 2, 29), RepeatRule.YEARLY)

        assertEquals(listOf(LocalDate.of(2027, 2, 28)), leap.occurrencesIn(YearMonth.of(2027, 2)))
        assertEquals(listOf(LocalDate.of(2028, 2, 29)), leap.occurrencesIn(YearMonth.of(2028, 2)))
    }

    @Test
    fun `매월 31일 반복은 없는 달에 말일 다음 달은 원래 날짜`() {
        val monthly = content(LocalDate.of(2026, 1, 31), RepeatRule.MONTHLY)

        assertEquals(listOf(LocalDate.of(2026, 4, 30)), monthly.occurrencesIn(YearMonth.of(2026, 4)))
        assertEquals(listOf(LocalDate.of(2026, 5, 31)), monthly.occurrencesIn(YearMonth.of(2026, 5)))
        assertEquals(listOf(LocalDate.of(2026, 2, 28)), monthly.occurrencesIn(YearMonth.of(2026, 2)))
    }

    @Test
    fun `기준일 이전 달에는 반복 도래일이 없음`() {
        val monthly = content(LocalDate.of(2026, 11, 20), RepeatRule.MONTHLY)

        assertTrue(monthly.occurrencesIn(YearMonth.of(2026, 10)).isEmpty())
        assertTrue(
            content(LocalDate.of(2026, 11, 20), RepeatRule.YEARLY).occurrencesIn(YearMonth.of(2025, 11)).isEmpty(),
        )
    }

    @Test
    fun `일정은 반복 값이 있어도 그 날짜 하나 날짜 없으면 없음`() {
        val plan = content(LocalDate.of(2026, 11, 6), RepeatRule.MONTHLY, ItemType.PLAN)

        assertEquals(listOf(LocalDate.of(2026, 11, 6)), plan.occurrencesIn(november))
        assertTrue(plan.occurrencesIn(YearMonth.of(2026, 12)).isEmpty())
        assertTrue(content(null, type = ItemType.PLAN).occurrencesIn(november).isEmpty())
    }
}
