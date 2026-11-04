package jpark.bro.days.feature.settings.impl.policylist

import app.cash.turbine.test
import jpark.bro.days.core.testing.MainDispatcherRule
import jpark.bro.days.feature.settings.impl.PolicyType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PolicyListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel = PolicyListViewModel()

    @Test
    fun `정책 문서를 누르면 그 문서를 엶`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(PolicyListAction.PolicyClicked(PolicyType.PRIVACY))
                assertEquals(PolicyListEvent.OpenPolicy(PolicyType.PRIVACY), awaitItem())
            }
        }

    @Test
    fun `오픈소스 라이선스를 누르면 라이선스 목록을 엶`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(PolicyListAction.OssLicensesClicked)
                assertEquals(PolicyListEvent.OpenOssLicenses, awaitItem())
            }
        }

    @Test
    fun `글꼴 라이선스는 시트로 열고 닫음`() {
        viewModel.onAction(PolicyListAction.FontLicenseClicked)
        assertTrue(viewModel.state.value.isFontLicenseOpen)

        viewModel.onAction(PolicyListAction.FontLicenseDismissed)
        assertFalse(viewModel.state.value.isFontLicenseOpen)
    }
}
