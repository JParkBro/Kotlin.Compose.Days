package jpark.bro.days.core.domain.dday

import jpark.bro.days.core.domain.SampleScenario.TODAY
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.RepeatRule
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CalculateDdayTest {
    private fun dday(
        date: LocalDate,
        countMode: CountMode = CountMode.DDAY,
        countFirstDayAsOne: Boolean = false,
        repeat: RepeatRule = RepeatRule.NONE,
        today: LocalDate = TODAY,
    ) = calculateDday(date, countMode, countFirstDayAsOne, repeat, today)

    // product-spec 9장 샘플 (오늘 = 2026.11.05)

    @Test
    fun `샘플 - 토익 시험은 D-3`() {
        assertEquals(DdayCount.Remaining(3), dday(LocalDate.of(2026, 11, 8)))
    }

    @Test
    fun `샘플 - 제주 여행(일정 디데이로 세기)은 D-12`() {
        assertEquals(DdayCount.Remaining(12), dday(LocalDate.of(2026, 11, 17)))
    }

    @Test
    fun `샘플 - 지민 생일(매년)은 D-45`() {
        assertEquals(DdayCount.Remaining(45), dday(LocalDate.of(1999, 12, 20), repeat = RepeatRule.YEARLY))
    }

    @Test
    fun `샘플 - 엄마 생신(매년)은 D-58`() {
        assertEquals(DdayCount.Remaining(58), dday(LocalDate.of(1970, 1, 2), repeat = RepeatRule.YEARLY))
    }

    @Test
    fun `샘플 - 전역일은 D-134`() {
        assertEquals(DdayCount.Remaining(134), dday(LocalDate.of(2027, 3, 19)))
    }

    @Test
    fun `샘플 - 금연(D+ 1일 세기)은 D+91`() {
        val result = dday(LocalDate.of(2026, 8, 7), countMode = CountMode.DPLUS, countFirstDayAsOne = true)

        assertEquals(DdayCount.Elapsed(91), result)
    }

    // D-Day 방식

    @Test
    fun `D-Day 방식 - 당일은 D-Day`() {
        assertEquals(DdayCount.Today, dday(TODAY))
    }

    @Test
    fun `D-Day 방식 - 기준일이 지나면 D+로 계속 셈`() {
        assertEquals(DdayCount.Elapsed(2), dday(TODAY.minusDays(2)))
    }

    @Test
    fun `D-Day 방식 - 1일 세기는 적용하지 않음`() {
        assertEquals(DdayCount.Elapsed(2), dday(TODAY.minusDays(2), countFirstDayAsOne = true))
    }

    // D+ 방식

    @Test
    fun `D+ 방식 - 1일 세기를 끄면 당일은 D-Day`() {
        assertEquals(DdayCount.Today, dday(TODAY, countMode = CountMode.DPLUS))
    }

    @Test
    fun `D+ 방식 - 1일 세기를 켜면 당일은 D+1`() {
        assertEquals(DdayCount.Elapsed(1), dday(TODAY, countMode = CountMode.DPLUS, countFirstDayAsOne = true))
    }

    @Test
    fun `D+ 방식 - 1일 세기를 끄면 지난 날 수 그대로`() {
        assertEquals(DdayCount.Elapsed(90), dday(LocalDate.of(2026, 8, 7), countMode = CountMode.DPLUS))
    }

    @Test
    fun `D+ 방식 - 기준일이 미래면 D-N`() {
        val result = dday(TODAY.plusDays(5), countMode = CountMode.DPLUS, countFirstDayAsOne = true)

        assertEquals(DdayCount.Remaining(5), result)
    }

    // 반복

    @Test
    fun `매년 반복 - 그날이면 D-Day`() {
        assertEquals(DdayCount.Today, dday(LocalDate.of(1990, 11, 5), repeat = RepeatRule.YEARLY))
    }

    @Test
    fun `매월 반복 - 다음 달 같은 날까지 카운트다운`() {
        assertEquals(DdayCount.Remaining(29), dday(LocalDate.of(2026, 1, 4), repeat = RepeatRule.MONTHLY))
    }
}
