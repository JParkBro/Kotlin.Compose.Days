package jpark.bro.days.feature.onboarding.impl.startmode

import app.cash.turbine.test
import jpark.bro.days.core.testing.MainDispatcherRule
import jpark.bro.days.feature.onboarding.api.OnboardingResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StartModeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel = StartModeViewModel()

    @Test
    fun `기본은 커플 연결로 끝냄`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(StartModeAction.NextClicked)
                assertEquals(StartModeEvent.Finished(OnboardingResult.Couple), awaitItem())
            }
        }

    @Test
    fun `혼자 시작을 고르면 솔로로 끝냄`() =
        runTest {
            viewModel.onAction(StartModeAction.ModeSelected(StartMode.SOLO))

            viewModel.event.test {
                viewModel.onAction(StartModeAction.NextClicked)
                assertEquals(StartModeEvent.Finished(OnboardingResult.Solo), awaitItem())
            }
        }
}
