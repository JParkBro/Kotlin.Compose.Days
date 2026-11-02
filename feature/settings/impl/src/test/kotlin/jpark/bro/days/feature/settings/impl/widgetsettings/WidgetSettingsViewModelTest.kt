package jpark.bro.days.feature.settings.impl.widgetsettings

import app.cash.turbine.test
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.WidgetConfig
import jpark.bro.days.core.model.WidgetSize
import jpark.bro.days.core.testing.FakeHomeWidgets
import jpark.bro.days.core.testing.FakeItemRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WidgetSettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val discharge =
        Item(
            id = "discharge",
            content = ItemContent(type = ItemType.DDAY, title = "전역일", colorKey = "PINK", iconKey = "FLAG"),
            ownerUserId = "user-me",
            updatedBy = "user-me",
            createdAt = 0L,
            updatedAt = 0L,
        )

    private val homeWidgets =
        FakeHomeWidgets(
            placed =
                listOf(
                    WidgetConfig(1, WidgetSize.SMALL, itemId = "discharge"),
                    WidgetConfig(2, WidgetSize.MEDIUM),
                    WidgetConfig(3, WidgetSize.SMALL, itemId = "deleted"),
                ),
        )
    private val viewModel by lazy { WidgetSettingsViewModel(homeWidgets, FakeItemRepository(listOf(discharge))) }

    @Test
    fun `놓인 위젯마다 표시할 디데이 이름을 붙임`() {
        val rows = viewModel.state.value.widgets

        assertEquals(listOf(1, 2, 3), rows.map { it.config.widgetId })
        assertEquals("전역일", rows[0].itemTitle)
        assertEquals(null, rows[1].itemTitle)
        assertFalse(rows[1].isMissing)
        assertTrue(rows[2].isMissing)
    }

    @Test
    fun `위젯을 누르면 그 위젯의 설정 화면을 엶`() {
        viewModel.onAction(WidgetSettingsAction.WidgetClicked(2))

        assertEquals(listOf(2), homeWidgets.openedConfigs)
    }

    @Test
    fun `위젯이 놓이거나 지워지면 목록이 바뀜`() {
        viewModel.state.value
        homeWidgets.placed.value = emptyList()

        assertTrue(
            viewModel.state.value.widgets
                .isEmpty(),
        )
    }

    @Test
    fun `위젯 추가는 크기를 고르면 디데이 없이 요청함`() {
        viewModel.onAction(WidgetSettingsAction.AddClicked)
        assertTrue(viewModel.state.value.isSizeSheetOpen)

        viewModel.onAction(WidgetSettingsAction.SizeSelected(WidgetSize.SMALL))

        assertFalse(viewModel.state.value.isSizeSheetOpen)
        assertEquals(listOf(null to WidgetSize.SMALL), homeWidgets.pinRequests)
    }

    @Test
    fun `런처가 추가를 지원하지 않으면 버튼이 없고 요청이 실패하면 안내함`() =
        runTest {
            homeWidgets.pinSupported = false
            homeWidgets.pinResult = false
            assertFalse(viewModel.state.value.canAddWidget)

            viewModel.event.test {
                viewModel.onAction(WidgetSettingsAction.SizeSelected(WidgetSize.MEDIUM))
                assertEquals(WidgetSettingsEvent.AddFailed, awaitItem())
            }
        }
}
