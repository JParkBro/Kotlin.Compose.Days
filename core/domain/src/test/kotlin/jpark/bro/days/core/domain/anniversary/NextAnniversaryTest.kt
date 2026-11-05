package jpark.bro.days.core.domain.anniversary

import jpark.bro.days.core.domain.SampleScenario.COUPLE_START
import jpark.bro.days.core.domain.SampleScenario.TODAY
import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.UserBirthday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class NextAnniversaryTest {
    private fun next(
        today: LocalDate = TODAY,
        start: LocalDate = COUPLE_START,
        birthdays: List<UserBirthday> = emptyList(),
        hidden: Set<String> = emptySet(),
    ) = nextAnniversary(start, countFirstDayAsOne = true, birthdays = birthdays, today = today, hiddenRuleKeys = hidden)

    @Test
    fun `샘플 - 다음 기념일은 600일, 500일부터 10일 지나 10퍼센트`() {
        val result = next()

        assertEquals(AutoAnniversary.Days(600, LocalDate.of(2027, 2, 3)), result?.anniversary)
        assertEquals(DdayCount.Remaining(90), result!!.dday)
        assertEquals(0.1f, result.progress, 0.0001f)
    }

    @Test
    fun `기념일 당일이면 그 기념일이고 진행 100퍼센트`() {
        val result = next(today = LocalDate.of(2026, 10, 26))

        assertEquals(AutoAnniversary.Days(500, LocalDate.of(2026, 10, 26)), result?.anniversary)
        assertEquals(DdayCount.Today, result!!.dday)
        assertEquals(1f, result.progress, 0f)
    }

    @Test
    fun `생일이 더 가까우면 생일`() {
        val result = next(birthdays = listOf(UserBirthday("user-jimin", LocalDate.of(1999, 11, 20))))

        assertEquals(AutoAnniversary.Birthday("user-jimin", 2026, LocalDate.of(2026, 11, 20)), result?.anniversary)
    }

    @Test
    fun `숨긴 기념일은 건너뛰고 직전 기념일부터 비율을 셈`() {
        val result = next(hidden = setOf("DAY_600"))

        assertEquals(AutoAnniversary.Days(700, LocalDate.of(2027, 5, 14)), result?.anniversary)
        assertEquals(10f / 200, result!!.progress, 0.0001f)
    }

    @Test
    fun `오늘 사귀기 시작했으면 사귄 날부터 0퍼센트`() {
        val result = next(start = TODAY)

        assertEquals(AutoAnniversary.Days(100, TODAY.plusDays(99)), result?.anniversary)
        assertEquals(0f, result!!.progress, 0f)
    }

    @Test
    fun `사귄 날이 아직 오지 않았으면 0퍼센트`() {
        val result = next(start = LocalDate.of(2026, 12, 1))

        assertEquals(0f, result!!.progress, 0f)
    }

    @Test
    fun `2년 안의 기념일을 모두 숨기면 없음`() {
        val hidden = (5..13).map { "DAY_${it * 100}" }.toSet() + setOf("YEAR_2", "YEAR_3")

        assertNull(next(hidden = hidden))
    }
}
