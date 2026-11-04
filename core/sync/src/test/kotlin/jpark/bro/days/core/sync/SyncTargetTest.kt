package jpark.bro.days.core.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyncTargetTest {
    private val user = MutableStateFlow<String?>(ME)
    private val foreground = MutableStateFlow(true)
    private val enabled = MutableStateFlow(true)

    @Test
    fun `로그인 · 포그라운드 · 동기화 켜짐일 때만 리스너 대상`() =
        runTest {
            assertEquals(ME, listenTarget(user, foreground, enabled).first())

            foreground.value = false
            assertNull(listenTarget(user, foreground, enabled).first())

            foreground.value = true
            enabled.value = false
            assertNull(listenTarget(user, foreground, enabled).first())

            enabled.value = true
            user.value = null
            assertNull(listenTarget(user, foreground, enabled).first())
        }

    @Test
    fun `Push · 백그라운드 조회는 포그라운드가 아니어도 되지만 동기화가 꺼지면 멈춤`() =
        runTest {
            foreground.value = false
            assertEquals(ME, backgroundTarget(user, enabled).first())

            enabled.value = false
            assertNull(backgroundTarget(user, enabled).first())
        }
}
