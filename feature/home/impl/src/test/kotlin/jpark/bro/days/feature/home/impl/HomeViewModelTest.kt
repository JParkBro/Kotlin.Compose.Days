package jpark.bro.days.feature.home.impl

import app.cash.turbine.test
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.testing.FakeItemRepository
import jpark.bro.days.core.testing.FakeSettingsRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** product-spec 9장: 솔로 전역일 */
    private val discharge =
        Item(
            id = "discharge",
            content =
                ItemContent(
                    type = ItemType.DDAY,
                    title = "전역일",
                    colorKey = "PINK",
                    iconKey = "FLAG",
                    date = LocalDate.of(2027, 3, 19),
                ),
            ownerUserId = "user-me",
            updatedBy = "user-me",
            createdAt = 0L,
            updatedAt = 0L,
        )

    private val settingsRepository = FakeSettingsRepository()

    private fun viewModel(items: List<Item> = emptyList()) =
        HomeViewModel(FakeItemRepository(items), settingsRepository)

    @Test
    fun `등록한 항목이 없으면 빈 상태와 커플 연결 카드를 보여줌`() {
        val state = viewModel().state.value

        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
        assertTrue(state.showCoupleBanner)
    }

    @Test
    fun `항목이 있으면 빈 상태가 아님`() {
        val state = viewModel(listOf(discharge)).state.value

        assertFalse(state.isEmpty)
        assertEquals(listOf(discharge), state.items)
    }

    @Test
    fun `빠른 시작 칩을 누르면 그 카테고리로 디데이 추가를 엶`() =
        runTest {
            val viewModel = viewModel()

            viewModel.event.test {
                viewModel.onAction(HomeAction.QuickStartClicked(QuickStart.DISCHARGE))
                assertEquals(HomeEvent.OpenItemEdit(ItemType.DDAY, QuickStart.DISCHARGE), awaitItem())
            }
        }

    @Test
    fun `디데이 추가 버튼을 누르면 빈 디데이 추가를 엶`() =
        runTest {
            val viewModel = viewModel()

            viewModel.event.test {
                viewModel.onAction(HomeAction.AddClicked(ItemType.DDAY))
                assertEquals(HomeEvent.OpenItemEdit(ItemType.DDAY), awaitItem())
            }
        }

    @Test
    fun `연결하기를 누르면 커플 연결 화면으로 감`() =
        runTest {
            val viewModel = viewModel()

            viewModel.event.test {
                viewModel.onAction(HomeAction.ConnectCoupleClicked)
                assertEquals(HomeEvent.OpenPairing, awaitItem())
            }
        }

    @Test
    fun `커플 연결 카드를 닫으면 저장되고 다시 보이지 않음`() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(HomeAction.CoupleBannerDismissed)

            assertFalse(viewModel.state.value.showCoupleBanner)
            assertTrue(settingsRepository.observeCoupleBannerDismissed().first())
            assertFalse(viewModel(listOf(discharge)).state.value.showCoupleBanner)
        }
}
