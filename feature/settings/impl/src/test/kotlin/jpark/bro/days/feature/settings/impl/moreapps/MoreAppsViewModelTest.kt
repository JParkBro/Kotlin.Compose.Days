package jpark.bro.days.feature.settings.impl.moreapps

import app.cash.turbine.test
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MoreAppsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel = MoreAppsViewModel()

    @Test
    fun `처음에는 불러오는 중이고 다 불러오면 페이지를 보여줌`() {
        assertEquals(MoreAppsState(isLoading = true), viewModel.state.value)

        viewModel.onAction(MoreAppsAction.PageLoaded)

        assertEquals(MoreAppsState(isLoading = false), viewModel.state.value)
    }

    @Test
    fun `실패하면 안내하고 다시 시도하면 처음부터 다시 불러옴`() {
        viewModel.onAction(MoreAppsAction.PageFailed)
        assertEquals(MoreAppsState(isLoading = false, isFailed = true), viewModel.state.value)

        viewModel.onAction(MoreAppsAction.RetryClicked)

        assertEquals(MoreAppsState(isLoading = true, isFailed = false, reloadCount = 1), viewModel.state.value)
    }

    @Test
    fun `스토어 링크는 스토어로 열고 뒤로는 닫음`() =
        runTest {
            val url = "https://play.google.com/store/apps/details?id=com.jparkbro.anipick"
            viewModel.event.test {
                viewModel.onAction(MoreAppsAction.StoreLinkClicked(url))
                assertEquals(MoreAppsEvent.OpenStore(url), awaitItem())
                viewModel.onAction(MoreAppsAction.BackClicked)
                assertEquals(MoreAppsEvent.NavigateBack, awaitItem())
            }
        }
}
