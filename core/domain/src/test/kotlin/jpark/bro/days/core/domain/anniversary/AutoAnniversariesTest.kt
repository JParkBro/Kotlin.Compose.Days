package jpark.bro.days.core.domain.anniversary

import jpark.bro.days.core.domain.SampleScenario.COUPLE_START
import jpark.bro.days.core.domain.SampleScenario.TODAY
import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.UserBirthday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AutoAnniversariesTest {
    private val sampleTimeline =
        autoAnniversaries(
            startDate = COUPLE_START,
            countFirstDayAsOne = true,
            birthdays = emptyList(),
            period = LocalDate.of(2026, 10, 1)..LocalDate.of(2027, 6, 30),
        )

    @Test
    fun `샘플 - 다가오는 기념일 타임라인 (1일 세기)`() {
        val expected =
            listOf(
                AutoAnniversary.Days(500, LocalDate.of(2026, 10, 26)),
                AutoAnniversary.Days(600, LocalDate.of(2027, 2, 3)),
                AutoAnniversary.Days(700, LocalDate.of(2027, 5, 14)),
                AutoAnniversary.Years(2, LocalDate.of(2027, 6, 14)),
            )

        assertEquals(expected, sampleTimeline)
    }

    @Test
    fun `샘플 - 오늘 이후 다음 기념일은 600일`() {
        val next = sampleTimeline.first { !it.date.isBefore(TODAY) }

        assertEquals(AutoAnniversary.Days(600, LocalDate.of(2027, 2, 3)), next)
    }

    @Test
    fun `1일 세기를 끄면 100일은 사귄 날 + 100일`() {
        val result = autoAnniversaries(COUPLE_START, false, emptyList(), COUPLE_START..COUPLE_START.plusDays(100))

        assertEquals(listOf(AutoAnniversary.Days(100, LocalDate.of(2025, 9, 22))), result)
    }

    @Test
    fun `1일 세기를 켜면 100일은 사귄 날 + 99일`() {
        val result = autoAnniversaries(COUPLE_START, true, emptyList(), COUPLE_START..COUPLE_START.plusDays(100))

        assertEquals(listOf(AutoAnniversary.Days(100, LocalDate.of(2025, 9, 21))), result)
    }

    @Test
    fun `주년은 1일 세기와 관계없이 같은 월일`() {
        val on = autoAnniversaries(COUPLE_START, true, emptyList(), LocalDate.of(2026, 6, 1)..LocalDate.of(2026, 6, 30))
        val off =
            autoAnniversaries(COUPLE_START, false, emptyList(), LocalDate.of(2026, 6, 1)..LocalDate.of(2026, 6, 30))

        assertEquals(listOf(AutoAnniversary.Years(1, LocalDate.of(2026, 6, 14))), on)
        assertEquals(on, off)
    }

    @Test
    fun `2월 29일에 사귀면 평년 주년은 2월 28일`() {
        val start = LocalDate.of(2024, 2, 29)

        val result = autoAnniversaries(start, true, emptyList(), LocalDate.of(2025, 2, 1)..LocalDate.of(2025, 3, 31))

        assertEquals(listOf(AutoAnniversary.Years(1, LocalDate.of(2025, 2, 28))), result)
    }

    @Test
    fun `생일은 매년 같은 월일, 키는 BDAY_uid_연도`() {
        val result =
            autoAnniversaries(
                startDate = COUPLE_START,
                countFirstDayAsOne = true,
                birthdays = listOf(UserBirthday("jimin", LocalDate.of(1999, 12, 20))),
                period = LocalDate.of(2026, 12, 1)..LocalDate.of(2026, 12, 31),
            )

        assertEquals(listOf(AutoAnniversary.Birthday("jimin", 2026, LocalDate.of(2026, 12, 20))), result)
        assertEquals("BDAY_jimin_2026", result.single().ruleKey)
    }

    @Test
    fun `2월 29일 생일은 평년에 2월 28일`() {
        val result =
            autoAnniversaries(
                startDate = COUPLE_START,
                countFirstDayAsOne = true,
                birthdays = listOf(UserBirthday("u1", LocalDate.of(2000, 2, 29))),
                period = LocalDate.of(2027, 2, 28)..LocalDate.of(2027, 2, 28),
            )

        assertEquals(listOf(AutoAnniversary.Birthday("u1", 2027, LocalDate.of(2027, 2, 28))), result)
    }

    @Test
    fun `숨긴 기념일은 빠진다`() {
        val result =
            autoAnniversaries(
                startDate = COUPLE_START,
                countFirstDayAsOne = true,
                birthdays = emptyList(),
                period = LocalDate.of(2026, 10, 1)..LocalDate.of(2027, 6, 30),
                hiddenRuleKeys = setOf("DAY_600", "YEAR_2"),
            )

        assertEquals(listOf("DAY_500", "DAY_700"), result.map { it.ruleKey })
    }

    @Test
    fun `사귄 날 이전 범위는 비어 있음`() {
        val result =
            autoAnniversaries(COUPLE_START, true, emptyList(), LocalDate.of(2020, 1, 1)..LocalDate.of(2025, 6, 1))

        assertTrue(result.isEmpty())
    }
}
