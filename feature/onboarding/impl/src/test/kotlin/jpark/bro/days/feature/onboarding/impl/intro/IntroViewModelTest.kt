package jpark.bro.days.feature.onboarding.impl.intro

import app.cash.turbine.test
import jpark.bro.days.core.testing.FakeOnboardingRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class IntroViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val onboardingRepository = FakeOnboardingRepository()

    @Test
    fun `처음이면 소개를 보여줌`() =
        runTest {
            val viewModel = IntroViewModel(onboardingRepository)

            viewModel.event.test {
                expectNoEvents()
            }
            assertFalse(viewModel.state.value.isLoading)
        }

    @Test
    fun `시작하면 본 것으로 남기고 로그인으로`() =
        runTest {
            val viewModel = IntroViewModel(onboardingRepository)

            viewModel.event.test {
                viewModel.onAction(IntroAction.StartClicked)
                assertEquals(IntroEvent.NavigateToLogin, awaitItem())
            }
            assertTrue(onboardingRepository.introSeen)
        }

    @Test
    fun `이미 봤으면 바로 로그인으로`() =
        runTest {
            onboardingRepository.introSeen = true
            val viewModel = IntroViewModel(onboardingRepository)

            viewModel.event.test {
                assertEquals(IntroEvent.NavigateToLogin, awaitItem())
            }
            assertTrue(viewModel.state.value.isLoading)
        }
}
