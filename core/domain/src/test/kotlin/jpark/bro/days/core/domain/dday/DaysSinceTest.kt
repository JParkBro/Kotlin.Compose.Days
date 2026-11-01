package jpark.bro.days.core.domain.dday

import jpark.bro.days.core.domain.SampleScenario
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DaysSinceTest {
    @Test
    fun `시작일을 1일로 세면 당일은 1일`() {
        val day = LocalDate.of(2026, 1, 1)

        assertEquals(1, daysSince(start = day, today = day, countFirstDay = true))
    }

    @Test
    fun `시작일을 1일로 세지 않으면 당일은 0일`() {
        val day = LocalDate.of(2026, 1, 1)

        assertEquals(0, daysSince(start = day, today = day, countFirstDay = false))
    }

    @Test
    fun `샘플 - 지민 현우는 함께한 지 510일`() {
        val result = daysSince(SampleScenario.COUPLE_START, SampleScenario.TODAY, countFirstDay = true)

        assertEquals(510, result)
    }

    @Test
    fun `윤년 2월 29일을 넘어가는 계산`() {
        val result = daysSince(LocalDate.of(2028, 2, 28), LocalDate.of(2028, 3, 1), countFirstDay = false)

        assertEquals(2, result)
    }

    @Test
    fun `평년 2월을 넘어가는 계산`() {
        val result = daysSince(LocalDate.of(2027, 2, 28), LocalDate.of(2027, 3, 1), countFirstDay = false)

        assertEquals(1, result)
    }

    @Test
    fun `연말을 넘어가는 계산`() {
        val result = daysSince(LocalDate.of(2026, 12, 31), LocalDate.of(2027, 1, 1), countFirstDay = true)

        assertEquals(2, result)
    }
}
