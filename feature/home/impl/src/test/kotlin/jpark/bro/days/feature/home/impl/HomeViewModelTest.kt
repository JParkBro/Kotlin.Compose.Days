package jpark.bro.days.feature.home.impl

import app.cash.turbine.test
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.Item
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
import java.time.Clock
import java.time.ZoneId

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val seoul = ZoneId.of("Asia/Seoul")
    private val clock = Clock.fixed(Today.atStartOfDay(seoul).toInstant(), seoul)
    private val settingsRepository = FakeSettingsRepository()
    private lateinit var itemRepository: FakeItemRepository

    private fun viewModel(items: List<Item> = emptyList()): HomeViewModel {
        itemRepository = FakeItemRepository(items)
        return HomeViewModel(itemRepository, settingsRepository, clock)
    }

    @Test
    fun `등록한 항목이 없으면 빈 상태와 커플 연결 카드를 보여줌`() {
        val state = viewModel().state.value

        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
        assertTrue(state.showCoupleBanner)
    }

    @Test
    fun `항목이 있으면 오늘 기준으로 히어로와 리스트를 만듦`() {
        val state = viewModel(listOf(Discharge, MomBirthday, SeongsuDinner)).state.value

        assertFalse(state.isEmpty)
        assertEquals(Today, state.today)
        assertEquals(
            DdayCount.Remaining(58),
            state.content.hero
                ?.row
                ?.dday,
        )
        assertEquals(listOf(Discharge), state.content.rows.map { it.item })
        assertEquals(SeongsuDinner, state.content.nextPlan)
    }

    @Test
    fun `대표를 고정하면 히어로가 바뀜`() =
        runTest {
            val viewModel = viewModel(listOf(Discharge, MomBirthday))

            itemRepository.setPinned(Discharge.id, pinned = true)

            assertEquals(
                Discharge,
                viewModel.state.value.content.hero
                    ?.row
                    ?.item,
            )
            assertTrue(
                viewModel.state.value.content.hero!!
                    .isPinned,
            )
        }

    @Test
    fun `디데이를 누르면 상세로 감`() =
        runTest {
            val viewModel = viewModel(listOf(Discharge))

            viewModel.event.test {
                viewModel.onAction(HomeAction.ItemClicked(Discharge.id, ItemType.DDAY))
                assertEquals(HomeEvent.OpenItemDetail(Discharge.id), awaitItem())
            }
        }

    @Test
    fun `일정을 누르면 바로 수정 화면으로 감`() =
        runTest {
            val viewModel = viewModel(listOf(SeongsuDinner))

            viewModel.event.test {
                viewModel.onAction(HomeAction.ItemClicked(SeongsuDinner.id, ItemType.PLAN))
                assertEquals(HomeEvent.OpenItemEdit(ItemType.PLAN, itemId = SeongsuDinner.id), awaitItem())
            }
        }

    @Test
    fun `플러스 버튼은 추가 시트를 열고 종류를 고르면 시트를 닫고 추가 화면을 엶`() =
        runTest {
            val viewModel = viewModel(listOf(Discharge))
            viewModel.onAction(HomeAction.FabClicked)
            assertTrue(viewModel.state.value.isAddSheetOpen)

            viewModel.event.test {
                viewModel.onAction(HomeAction.AddClicked(ItemType.PLAN))
                assertEquals(HomeEvent.OpenItemEdit(ItemType.PLAN), awaitItem())
            }
            assertFalse(viewModel.state.value.isAddSheetOpen)
        }

    @Test
    fun `추가 시트를 닫으면 아무 데도 가지 않음`() {
        val viewModel = viewModel(listOf(Discharge))
        viewModel.onAction(HomeAction.FabClicked)

        viewModel.onAction(HomeAction.AddSheetDismissed)

        assertFalse(viewModel.state.value.isAddSheetOpen)
    }

    @Test
    fun `빠른 시작 칩을 누르면 그 카테고리로 디데이 추가를 엶`() =
        runTest {
            val viewModel = viewModel()

            viewModel.event.test {
                viewModel.onAction(HomeAction.QuickStartClicked(QuickStart.DISCHARGE))
                assertEquals(HomeEvent.OpenItemEdit(ItemType.DDAY, quickStart = QuickStart.DISCHARGE), awaitItem())
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
            assertFalse(viewModel(listOf(Discharge)).state.value.showCoupleBanner)
        }
}
