package jpark.bro.days.core.data.image

import org.junit.Assert.assertEquals
import org.junit.Test

class ScaledSizeTest {
    @Test
    fun `긴 변을 1080으로 줄이고 비율은 지킴`() {
        assertEquals(1080 to 810, scaledSize(4032, 3024, 1080))
        assertEquals(608 to 1080, scaledSize(2268, 4032, 1080))
    }

    @Test
    fun `작은 사진은 키우지 않고 경계값은 그대로`() {
        assertEquals(800 to 600, scaledSize(800, 600, 1080))
        assertEquals(1080 to 1080, scaledSize(1080, 1080, 1080))
    }

    @Test
    fun `아주 긴 사진도 짧은 변이 0이 되지 않음`() {
        assertEquals(1080 to 1, scaledSize(100_000, 10, 1080))
    }
}
