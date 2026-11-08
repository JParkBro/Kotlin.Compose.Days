package jpark.bro.days.feature.settings.impl.policydoc

import app.cash.turbine.test
import jpark.bro.days.core.testing.MainDispatcherRule
import jpark.bro.days.feature.settings.api.PolicyDocKey
import jpark.bro.days.feature.settings.api.PolicyType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PolicyDocViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel = PolicyDocViewModel(PolicyDocKey(PolicyType.PRIVACY))

    @Test
    fun `불러오는 중으로 시작하고 페이지가 뜨면 로딩을 끝냄`() {
        assertEquals(PolicyType.PRIVACY, viewModel.state.value.type)
        assertTrue(viewModel.state.value.isLoading)

        viewModel.onAction(PolicyDocAction.PageLoaded)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `불러오지 못하면 실패를 보여주고 다시 시도하면 새로 불러옴`() {
        viewModel.onAction(PolicyDocAction.PageFailed)
        assertTrue(viewModel.state.value.isFailed)

        viewModel.onAction(PolicyDocAction.RetryClicked)
        val state = viewModel.state.value
        assertFalse(state.isFailed)
        assertTrue(state.isLoading)
        assertEquals(1, state.reloadCount)
    }

    @Test
    fun `뒤로 가기`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(PolicyDocAction.BackClicked)
                assertEquals(PolicyDocEvent.NavigateBack, awaitItem())
            }
        }
}
