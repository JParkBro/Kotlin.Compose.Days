package jpark.bro.days.widget

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

class MidnightDelayTest {
    private val seoul = ZoneId.of("Asia/Seoul")

    @Test
    fun `오늘 오후 11시 59분이면 2분 뒤 (자정 + 1분)`() {
        val now = LocalDateTime.of(2026, 11, 5, 23, 59).atZone(seoul)

        assertEquals(Duration.ofMinutes(2), delayUntilNextMidnight(now))
    }

    @Test
    fun `자정 정각이면 다음 날 자정까지 기다림`() {
        val now = LocalDateTime.of(2026, 11, 5, 0, 0).atZone(seoul)

        assertEquals(Duration.ofHours(24).plusMinutes(1), delayUntilNextMidnight(now))
    }

    @Test
    fun `윤년 2월 28일 밤은 2월 29일 자정으로`() {
        val now = LocalDateTime.of(2028, 2, 28, 22, 0).atZone(seoul)

        assertEquals(Duration.ofHours(2).plusMinutes(1), delayUntilNextMidnight(now))
    }

    @Test
    fun `서머타임이 끝나는 날은 25시간으로 셈`() {
        // 미국 동부: 2026-11-01 02:00에 1시간 되돌림
        val now = LocalDateTime.of(2026, 11, 1, 0, 0).atZone(ZoneId.of("America/New_York"))

        assertEquals(Duration.ofHours(25).plusMinutes(1), delayUntilNextMidnight(now))
    }
}
