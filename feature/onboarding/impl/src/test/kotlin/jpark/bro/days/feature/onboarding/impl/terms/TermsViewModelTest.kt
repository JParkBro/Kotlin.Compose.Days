package jpark.bro.days.feature.onboarding.impl.terms

import app.cash.turbine.test
import jpark.bro.days.core.testing.FakeOnboardingRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import jpark.bro.days.feature.settings.api.PolicyType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TermsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val onboardingRepository = FakeOnboardingRepository()
    private val viewModel by lazy { TermsViewModel(onboardingRepository) }

    private fun checkRequired() {
        listOf(Term.AGE_OVER_14, Term.SERVICE, Term.PRIVACY).forEach { viewModel.onAction(TermsAction.Toggled(it)) }
    }

    @Test
    fun `필수 항목을 모두 체크해야 동의할 수 있음`() {
        viewModel.onAction(TermsAction.Toggled(Term.AGE_OVER_14))
        viewModel.onAction(TermsAction.Toggled(Term.SERVICE))
        viewModel.onAction(TermsAction.Toggled(Term.MARKETING))
        assertFalse(viewModel.state.value.canAgree)

        viewModel.onAction(TermsAction.Toggled(Term.PRIVACY))
        assertTrue(viewModel.state.value.canAgree)
        assertTrue(viewModel.state.value.allChecked)
    }

    @Test
    fun `전체 동의는 모두 체크하고 다시 누르면 모두 풂`() {
        viewModel.onAction(TermsAction.Toggled(Term.SERVICE))

        viewModel.onAction(TermsAction.AllToggled)
        assertEquals(Term.entries.toSet(), viewModel.state.value.checked)

        viewModel.onAction(TermsAction.AllToggled)
        assertEquals(emptySet<Term>(), viewModel.state.value.checked)
    }

    @Test
    fun `동의하면 마케팅 선택을 저장하고 시작 방식으로`() =
        runTest {
            checkRequired()

            viewModel.event.test {
                viewModel.onAction(TermsAction.AgreeClicked)
                assertEquals(TermsEvent.NavigateToStartMode, awaitItem())
            }
            assertEquals(listOf(false), onboardingRepository.marketingAgreements)
        }

    @Test
    fun `필수 항목이 빠지면 동의를 눌러도 저장하지 않음`() =
        runTest {
            viewModel.onAction(TermsAction.Toggled(Term.SERVICE))

            viewModel.event.test {
                viewModel.onAction(TermsAction.AgreeClicked)
                expectNoEvents()
            }
            assertEquals(emptyList<Boolean>(), onboardingRepository.marketingAgreements)
        }

    @Test
    fun `저장에 실패하면 알리고 다시 누를 수 있음`() =
        runTest {
            onboardingRepository.failNext = true
            viewModel.onAction(TermsAction.AllToggled)

            viewModel.event.test {
                viewModel.onAction(TermsAction.AgreeClicked)
                assertEquals(TermsEvent.AgreeFailed, awaitItem())
            }
            assertTrue(viewModel.state.value.canAgree)
        }

    @Test
    fun `보기를 누르면 정책 문서를 엶`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(TermsAction.ViewPolicyClicked(PolicyType.PRIVACY))
                assertEquals(TermsEvent.OpenPolicy(PolicyType.PRIVACY), awaitItem())
            }
        }
}
