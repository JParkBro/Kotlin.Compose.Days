package jpark.bro.days.feature.item.impl.itemdetail

import app.cash.turbine.test
import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.WidgetSize
import jpark.bro.days.core.testing.FakeHomeWidgets
import jpark.bro.days.core.testing.FakeItemRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import jpark.bro.days.feature.item.api.ItemDetailKey
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

/** product-spec 9장 시나리오 (오늘 = 2026.11.05) */
class ItemDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val seoul = ZoneId.of("Asia/Seoul")
    private val today = LocalDate.of(2026, 11, 5)
    private val clock = Clock.fixed(today.atStartOfDay(seoul).toInstant(), seoul)

    private fun item(
        id: String,
        content: ItemContent,
    ) = Item(id, content, "user-me", "user-me", 0L, 0L)

    private fun dday(
        title: String,
        date: LocalDate,
        countMode: CountMode = CountMode.DDAY,
    ) = ItemContent(
        type = ItemType.DDAY,
        title = title,
        colorKey = "PINK",
        iconKey = "STAR",
        date = date,
        countMode = countMode,
    )

    /** 사귄 날 2026.01.01 (D+, 1일 세기 없음): 300일 지남, 다음은 1주년 */
    private val together = item("together", dday("사귄 날", LocalDate.of(2026, 1, 1), CountMode.DPLUS))

    /** 전역일 D-134 */
    private val discharge = item("discharge", dday("전역일", LocalDate.of(2027, 3, 19)))

    private val repository = FakeItemRepository(listOf(together, discharge))
    private val homeWidgets = FakeHomeWidgets()

    private fun viewModel(id: String) = ItemDetailViewModel(ItemDetailKey(id), repository, homeWidgets, clock)

    @Test
    fun `항목과 오늘 기준 D-day를 불러옴`() {
        val state = viewModel(discharge.id).state.value

        assertFalse(state.isLoading)
        assertEquals(discharge, state.item)
        assertEquals(DdayCount.Remaining(134), state.dday)
    }

    @Test
    fun `지난 날을 세는 디데이는 지난 1개와 다가오는 3개 타임라인을 보여주고 다음 것을 강조함`() {
        val milestones = viewModel(together.id).state.value.milestones

        assertEquals(listOf("DAY_300", "YEAR_1", "DAY_400", "DAY_500"), milestones.map { it.anniversary.ruleKey })
        assertEquals(listOf(true, false, false, false), milestones.map { it.isPassed })
        assertEquals(listOf(false, true, false, false), milestones.map { it.isNext })
        assertEquals(DdayCount.Remaining(57), milestones[1].dday)
        assertTrue(milestones[1].anniversary is AutoAnniversary.Years)
    }

    @Test
    fun `카운트다운 디데이는 타임라인이 없음`() {
        assertTrue(
            viewModel(discharge.id)
                .state.value.milestones
                .isEmpty(),
        )
    }

    @Test
    fun `수정하면 바로 반영됨`() =
        runTest {
            val viewModel = viewModel(discharge.id)

            repository.saveItem(discharge.id, discharge.content.copy(title = "전역 축하"))

            assertEquals(
                "전역 축하",
                viewModel.state.value.item
                    ?.content
                    ?.title,
            )
        }

    @Test
    fun `수정 버튼은 해당 항목 수정 화면을 엶`() =
        runTest {
            val viewModel = viewModel(discharge.id)

            viewModel.event.test {
                viewModel.onAction(ItemDetailAction.EditClicked)
                assertEquals(ItemDetailEvent.OpenEdit(discharge.id, ItemType.DDAY), awaitItem())
            }
        }

    @Test
    fun `항목이 삭제되면 뒤로 감`() =
        runTest {
            val viewModel = viewModel(discharge.id)

            viewModel.event.test {
                repository.deleteItem(discharge.id)
                assertEquals(ItemDetailEvent.NavigateBack, awaitItem())
            }
        }

    @Test
    fun `없는 항목이면 뒤로 감`() =
        runTest {
            val viewModel = viewModel("missing")

            viewModel.event.test { assertEquals(ItemDetailEvent.NavigateBack, awaitItem()) }
        }

    @Test
    fun `위젯 추가를 지원하면 버튼이 보이고 크기를 고르면 그 디데이로 추가를 요청함`() =
        runTest {
            val viewModel = viewModel(discharge.id)
            assertTrue(viewModel.state.value.canAddWidget)

            viewModel.onAction(ItemDetailAction.AddWidgetClicked)
            assertTrue(viewModel.state.value.isWidgetSheetOpen)

            viewModel.onAction(ItemDetailAction.WidgetSizeSelected(WidgetSize.MEDIUM))

            assertFalse(viewModel.state.value.isWidgetSheetOpen)
            assertEquals(listOf(discharge.id to WidgetSize.MEDIUM), homeWidgets.pinRequests)
        }

    @Test
    fun `위젯 추가를 지원하지 않으면 버튼이 없음`() {
        homeWidgets.pinSupported = false

        assertFalse(viewModel(discharge.id).state.value.canAddWidget)
    }

    @Test
    fun `위젯 추가 요청이 실패하면 안내함`() =
        runTest {
            val viewModel = viewModel(discharge.id)
            homeWidgets.pinResult = false

            viewModel.event.test {
                viewModel.onAction(ItemDetailAction.WidgetSizeSelected(WidgetSize.SMALL))
                assertEquals(ItemDetailEvent.WidgetAddFailed, awaitItem())
            }
        }
}
