package jpark.bro.days.core.data.repository

import jpark.bro.days.core.model.NotificationSettings
import jpark.bro.days.core.testing.testSettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultSettingsRepositoryTest {
    private val repository = DefaultSettingsRepository(testSettingsDataStore())

    @Test
    fun `알림 설정 기본값은 모두 켜짐 · 오전 9시`() =
        runTest {
            assertEquals(
                NotificationSettings(anniversaryEnabled = true, planEnabled = true, sendHour = 9),
                repository.observeNotificationSettings().first(),
            )
        }

    @Test
    fun `알림 설정을 저장하면 그대로 나옴`() =
        runTest {
            val settings = NotificationSettings(anniversaryEnabled = false, planEnabled = true, sendHour = 12)

            assertTrue(repository.setNotificationSettings(settings).isSuccess)

            assertEquals(settings, repository.observeNotificationSettings().first())
        }

    @Test
    fun `고를 수 없는 시각은 저장하지 않음`() =
        runTest {
            assertTrue(repository.setNotificationSettings(NotificationSettings(sendHour = 7)).isFailure)
            assertTrue(repository.setNotificationSettings(NotificationSettings(sendHour = 13)).isFailure)

            assertEquals(9, repository.observeNotificationSettings().first().sendHour)
        }
}
