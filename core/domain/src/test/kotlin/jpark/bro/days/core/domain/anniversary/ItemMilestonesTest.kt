package jpark.bro.days.core.domain.anniversary

import jpark.bro.days.core.domain.SampleScenario.TODAY
import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ItemMilestonesTest {
    private fun dday(
        date: LocalDate?,
        countMode: CountMode = CountMode.DPLUS,
        countFirstDayAsOne: Boolean = false,
    ) = ItemContent(
        type = ItemType.DDAY,
        title = "t",
        colorKey = "PINK",
        iconKey = "STAR",
        date = date,
        countMode = countMode,
        countFirstDayAsOne = countFirstDayAsOne,
    )

    private fun keys(list: List<AutoAnniversary>) = list.map { it.ruleKey to it.date }

    @Test
    fun `샘플 - 금연(1일 세기)은 지난 것 없이 100일 200일 300일`() {
        val milestones = itemMilestones(dday(LocalDate.of(2026, 8, 7), countFirstDayAsOne = true), TODAY)

        assertEquals(
            listOf(
                "DAY_100" to LocalDate.of(2026, 11, 14),
                "DAY_200" to LocalDate.of(2027, 2, 22),
                "DAY_300" to LocalDate.of(2027, 6, 2),
            ),
            keys(milestones),
        )
    }

    @Test
    fun `바로 직전에 지난 기념일 1개와 다가오는 3개`() {
        val milestones = itemMilestones(dday(LocalDate.of(2026, 1, 1)), TODAY)

        assertEquals(
            listOf(
                "DAY_300" to LocalDate.of(2026, 10, 28),
                "YEAR_1" to LocalDate.of(2027, 1, 1),
                "DAY_400" to LocalDate.of(2027, 2, 5),
                "DAY_500" to LocalDate.of(2027, 5, 16),
            ),
            keys(milestones),
        )
    }

    @Test
    fun `오늘이 기념일이면 다가오는 쪽 첫 번째`() {
        val milestones = itemMilestones(dday(LocalDate.of(2026, 7, 28)), TODAY)

        assertEquals("DAY_100" to TODAY, keys(milestones).first())
    }

    @Test
    fun `1일 세기는 D-Day 방식에서 적용하지 않음`() {
        val milestones =
            itemMilestones(dday(LocalDate.of(2026, 8, 7), CountMode.DDAY, countFirstDayAsOne = true), TODAY)

        assertEquals("DAY_100" to LocalDate.of(2026, 11, 15), keys(milestones).first())
    }

    @Test
    fun `윤년 2월 29일 시작은 평년 주년이 2월 28일`() {
        val milestones = itemMilestones(dday(LocalDate.of(2024, 2, 29)), LocalDate.of(2025, 2, 1))

        assertTrue("YEAR_1" to LocalDate.of(2025, 2, 28) in keys(milestones))
    }

    @Test
    fun `기준일이 아직 오지 않았거나 반복 일정 날짜 없는 항목은 타임라인 없음`() {
        assertTrue(itemMilestones(dday(LocalDate.of(2027, 3, 19)), TODAY).isEmpty())
        assertTrue(itemMilestones(dday(LocalDate.of(2000, 1, 2)).copy(repeatRule = RepeatRule.YEARLY), TODAY).isEmpty())
        assertTrue(itemMilestones(dday(LocalDate.of(2026, 1, 1)).copy(type = ItemType.PLAN), TODAY).isEmpty())
        assertTrue(itemMilestones(dday(null), TODAY).isEmpty())
    }
}
