package jpark.bro.days.core.notification

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderDelayTest {
    private val seoul = ZoneId.of("Asia/Seoul")
    private val nine = LocalTime.of(9, 0)

    @Test
    fun `9시 전이면 오늘 9시까지 기다림`() {
        val now = LocalDateTime.of(2026, 11, 5, 8, 30).atZone(seoul)

        assertEquals(Duration.ofMinutes(30), delayUntilNextReminder(now, nine))
    }

    @Test
    fun `9시 정각이면 다음 날 9시까지 기다림`() {
        val now = LocalDateTime.of(2026, 11, 5, 9, 0).atZone(seoul)

        assertEquals(Duration.ofHours(24), delayUntilNextReminder(now, nine))
    }

    @Test
    fun `9시가 지났으면 다음 날 9시`() {
        val now = LocalDateTime.of(2026, 11, 5, 21, 0).atZone(seoul)

        assertEquals(Duration.ofHours(12), delayUntilNextReminder(now, nine))
    }

    @Test
    fun `윤년 2월 28일 밤은 2월 29일 9시로`() {
        val now = LocalDateTime.of(2028, 2, 28, 22, 0).atZone(seoul)

        assertEquals(Duration.ofHours(11), delayUntilNextReminder(now, nine))
    }

    @Test
    fun `서머타임이 끝나는 밤은 한 시간 더 기다림`() {
        // 미국 동부: 2026-11-01 02:00에 1시간 되돌림
        val now = LocalDateTime.of(2026, 10, 31, 21, 0).atZone(ZoneId.of("America/New_York"))

        assertEquals(Duration.ofHours(13), delayUntilNextReminder(now, nine))
    }

    @Test
    fun `고른 시각이 정오면 정오 기준`() {
        val now = LocalDateTime.of(2026, 11, 5, 9, 30).atZone(seoul)

        assertEquals(Duration.ofMinutes(150), delayUntilNextReminder(now, LocalTime.NOON))
    }
}
