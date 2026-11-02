package jpark.bro.days.feature.calendar.impl

import jpark.bro.days.core.model.DdayCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarMonthTest {
    private val november = YearMonth.of(2026, 11)
    private val all = listOf(Toeic, Allowance, MomBirthday, SeongsuDinner, Lunch, Someday)

    private fun month(
        selected: LocalDate? = null,
        month: YearMonth = november,
    ) = buildCalendarMonth(all, month, selected, Today)

    private fun day(date: LocalDate) = month().weeks.flatten().single { it.date == date }

    @Test
    fun `그리드는 일요일부터 토요일까지 이전 다음 달 날짜를 포함함`() {
        val weeks = month().weeks

        // 2026년 11월 1일은 일요일, 30일은 월요일 → 5주
        assertEquals(5, weeks.size)
        assertTrue(weeks.all { it.size == 7 })
        assertEquals(LocalDate.of(2026, 11, 1), weeks.first().first().date)
        assertEquals(LocalDate.of(2026, 12, 5), weeks.last().last().date)
        assertTrue(weeks.all { it.first().date.dayOfWeek == DayOfWeek.SUNDAY })
        assertFalse(weeks.last().last().inMonth)
    }

    @Test
    fun `1일이 토요일인 달은 앞 달 날짜로 시작하고 6주`() {
        // 2026년 8월 1일은 토요일
        val weeks = month(month = YearMonth.of(2026, 8)).weeks

        assertEquals(LocalDate.of(2026, 7, 26), weeks.first().first().date)
        assertEquals(6, weeks.size)
    }

    @Test
    fun `디데이는 primary 점 일정은 accent 점`() {
        assertTrue(day(LocalDate.of(2026, 11, 8)).hasDday)
        assertFalse(day(LocalDate.of(2026, 11, 8)).hasPlan)
        assertTrue(day(LocalDate.of(2026, 11, 6)).hasPlan)
        assertTrue(day(LocalDate.of(2026, 11, 17)).hasDday)
    }

    @Test
    fun `오늘과 선택한 날을 표시함`() {
        assertTrue(day(Today).isToday)
        assertTrue(
            month(
                selected = LocalDate.of(2026, 11, 6),
            ).weeks.flatten().single { it.date == LocalDate.of(2026, 11, 6) }.isSelected,
        )
    }

    @Test
    fun `날짜를 고르지 않으면 이번 달 전체를 날짜 시각 순으로`() {
        val entries = month().entries

        assertEquals(listOf("점심 약속", "성수 저녁", "토익 시험", "용돈 날"), entries.map { it.item.content.title })
        assertEquals(LocalDate.of(2026, 11, 17), entries.last().date)
    }

    @Test
    fun `날짜를 고르면 그날 항목만`() {
        assertEquals(
            listOf("점심 약속", "성수 저녁"),
            month(selected = LocalDate.of(2026, 11, 6)).entries.map { it.item.content.title },
        )
        assertTrue(month(selected = LocalDate.of(2026, 11, 2)).entries.isEmpty())
    }

    @Test
    fun `디데이 줄에는 오늘 기준 D-day 일정 줄에는 없음`() {
        val entries = month().entries

        assertEquals(DdayCount.Remaining(3), entries.single { it.item == Toeic }.dday)
        assertNull(entries.single { it.item == SeongsuDinner }.dday)
    }

    @Test
    fun `이전 다음 달 칸에는 점이 없음`() {
        val december5 = month().weeks.flatten().single { it.date == LocalDate.of(2026, 12, 5) }

        assertFalse(december5.hasDday || december5.hasPlan)
    }
}
