package jpark.bro.days.feature.settings.impl.notificationsettings

import app.cash.turbine.test
import jpark.bro.days.core.model.MarketingConsent
import jpark.bro.days.core.model.NotificationSettings
import jpark.bro.days.core.testing.FakeOnboardingRepository
import jpark.bro.days.core.testing.FakeSettingsRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

/** product-spec 9장 시나리오: 오늘 = 2026.11.05 */
class NotificationSettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val seoul = ZoneId.of("Asia/Seoul")
    private val today = LocalDate.of(2026, 11, 5)
    private val clock = Clock.fixed(today.atTime(10, 0).atZone(seoul).toInstant(), seoul)
    private val settingsRepository = FakeSettingsRepository()
    private val onboardingRepository =
        FakeOnboardingRepository().apply {
            // 2026.06.14 약관 동의 때 마케팅 수신에 동의
            marketingConsent =
                MarketingConsent(
                    agreed = true,
                    updatedAt =
                        LocalDate
                            .of(2026, 6, 14)
                            .atTime(12, 0)
                            .atZone(seoul)
                            .toInstant()
                            .toEpochMilli(),
                )
            now = clock.millis()
        }
    private val viewModel by lazy { NotificationSettingsViewModel(settingsRepository, onboardingRepository, clock) }

    @Test
    fun `처음에는 저장된 알림 설정과 마케팅 동의 일자를 보여줌`() {
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(NotificationSettings(), state.settings)
        assertEquals(MarketingRow.Loaded(agreed = true, agreedOn = LocalDate.of(2026, 6, 14)), state.marketing)
    }

    @Test
    fun `기념일 일정 알림을 끄면 기기 설정에 저장됨`() =
        runTest {
            viewModel.onAction(NotificationSettingsAction.AnniversaryToggled(false))
            viewModel.onAction(NotificationSettingsAction.PlanToggled(false))

            val saved = settingsRepository.observeNotificationSettings().first()
            assertEquals(NotificationSettings(anniversaryEnabled = false, planEnabled = false), saved)
            assertEquals(saved, viewModel.state.value.settings)
        }

    @Test
    fun `알림 시간을 고르면 시트를 닫고 저장함`() =
        runTest {
            viewModel.onAction(NotificationSettingsAction.SendHourClicked)
            assertTrue(viewModel.state.value.isHourSheetVisible)

            viewModel.onAction(NotificationSettingsAction.SendHourSelected(12))

            assertFalse(viewModel.state.value.isHourSheetVisible)
            assertEquals(12, settingsRepository.observeNotificationSettings().first().sendHour)
        }

    @Test
    fun `알림 설정 저장에 실패하면 알림`() =
        runTest {
            viewModel.event.test {
                settingsRepository.failNextSave = true
                viewModel.onAction(NotificationSettingsAction.PlanToggled(false))

                assertEquals(NotificationSettingsEvent.SaveFailed, awaitItem())
            }
            assertTrue(viewModel.state.value.settings.planEnabled)
        }

    @Test
    fun `마케팅 동의를 철회하면 처리 날짜를 알리고 동의 일자를 지움`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(NotificationSettingsAction.MarketingToggled(false))

                assertEquals(NotificationSettingsEvent.MarketingChanged(agreed = false, date = today), awaitItem())
            }
            assertEquals(MarketingRow.Loaded(agreed = false, agreedOn = null), viewModel.state.value.marketing)
            assertEquals(
                MarketingConsent(agreed = false, updatedAt = clock.millis()),
                onboardingRepository.marketingConsent,
            )
        }

    @Test
    fun `마케팅 동의 변경에 실패하면 원래 값으로 두고 알림`() =
        runTest {
            val before = viewModel.state.value.marketing
            viewModel.event.test {
                onboardingRepository.failNext = true
                viewModel.onAction(NotificationSettingsAction.MarketingToggled(false))

                assertEquals(NotificationSettingsEvent.MarketingSaveFailed, awaitItem())
            }
            assertEquals(before, viewModel.state.value.marketing)
        }

    @Test
    fun `마케팅 동의를 읽지 못하면 다시 시도할 수 있음`() {
        onboardingRepository.failNext = true

        assertEquals(MarketingRow.Failed, viewModel.state.value.marketing)

        viewModel.onAction(NotificationSettingsAction.MarketingRetryClicked)

        assertEquals(
            MarketingRow.Loaded(agreed = true, agreedOn = LocalDate.of(2026, 6, 14)),
            viewModel.state.value.marketing,
        )
    }

    @Test
    fun `기기 알림 카드를 누르면 시스템 설정을 열고 확인한 상태를 보여줌`() =
        runTest {
            viewModel.onAction(NotificationSettingsAction.NotificationsChecked(false))
            assertFalse(viewModel.state.value.notificationsEnabled)

            viewModel.event.test {
                viewModel.onAction(NotificationSettingsAction.DeviceSettingsClicked)
                assertEquals(NotificationSettingsEvent.OpenDeviceSettings, awaitItem())
            }
        }
}
