package jpark.bro.days.core.domain.dday

import jpark.bro.days.core.model.RepeatRule
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class NextOccurrenceTest {
    @Test
    fun `반복이 없으면 기준일 그대로`() {
        val date = LocalDate.of(2020, 5, 1)

        assertEquals(date, nextOccurrence(date, RepeatRule.NONE, today = LocalDate.of(2026, 11, 5)))
    }

    @Test
    fun `매년 반복 - 올해 날짜가 지났으면 내년`() {
        val result = nextOccurrence(LocalDate.of(1970, 3, 1), RepeatRule.YEARLY, today = LocalDate.of(2026, 11, 5))

        assertEquals(LocalDate.of(2027, 3, 1), result)
    }

    @Test
    fun `매년 반복 - 오늘이 그날이면 오늘`() {
        val result = nextOccurrence(LocalDate.of(1990, 11, 5), RepeatRule.YEARLY, today = LocalDate.of(2026, 11, 5))

        assertEquals(LocalDate.of(2026, 11, 5), result)
    }

    @Test
    fun `매년 반복 - 기준일이 미래면 기준일`() {
        val result = nextOccurrence(LocalDate.of(2026, 12, 20), RepeatRule.YEARLY, today = LocalDate.of(2026, 11, 5))

        assertEquals(LocalDate.of(2026, 12, 20), result)
    }

    @Test
    fun `매년 반복 2월 29일 - 평년에는 2월 28일`() {
        val result = nextOccurrence(LocalDate.of(2024, 2, 29), RepeatRule.YEARLY, today = LocalDate.of(2026, 11, 5))

        assertEquals(LocalDate.of(2027, 2, 28), result)
    }

    @Test
    fun `매년 반복 2월 29일 - 윤년에는 2월 29일`() {
        val result = nextOccurrence(LocalDate.of(2024, 2, 29), RepeatRule.YEARLY, today = LocalDate.of(2028, 1, 10))

        assertEquals(LocalDate.of(2028, 2, 29), result)
    }

    @Test
    fun `매월 반복 31일 - 30일까지인 달은 말일`() {
        val result = nextOccurrence(LocalDate.of(2026, 1, 31), RepeatRule.MONTHLY, today = LocalDate.of(2026, 4, 1))

        assertEquals(LocalDate.of(2026, 4, 30), result)
    }

    @Test
    fun `매월 반복 31일 - 말일 다음 달은 원래 날짜로 돌아감`() {
        val result = nextOccurrence(LocalDate.of(2026, 1, 31), RepeatRule.MONTHLY, today = LocalDate.of(2026, 5, 1))

        assertEquals(LocalDate.of(2026, 5, 31), result)
    }

    @Test
    fun `매월 반복 31일 - 평년 2월은 28일`() {
        val result = nextOccurrence(LocalDate.of(2026, 1, 31), RepeatRule.MONTHLY, today = LocalDate.of(2027, 2, 1))

        assertEquals(LocalDate.of(2027, 2, 28), result)
    }
}
