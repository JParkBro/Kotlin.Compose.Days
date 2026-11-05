package jpark.bro.days.widget.config

import app.cash.turbine.test
import jpark.bro.days.core.model.AppThemeMode
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.ThemeSetting
import jpark.bro.days.core.model.UserProfile
import jpark.bro.days.core.model.WidgetBackground
import jpark.bro.days.core.model.WidgetConfig
import jpark.bro.days.core.model.WidgetSize
import jpark.bro.days.core.testing.FakeImageRepository
import jpark.bro.days.core.testing.FakeItemRepository
import jpark.bro.days.core.testing.FakeProfileRepository
import jpark.bro.days.core.testing.FakeSettingsRepository
import jpark.bro.days.core.testing.FakeWidgetConfigRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import jpark.bro.days.widget.WidgetDisplay
import jpark.bro.days.widget.WidgetPhotos
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

/** product-spec 9장 시나리오 (오늘 = 2026.11.05) */
class WidgetConfigViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val seoul = ZoneId.of("Asia/Seoul")
    private val clock = Clock.fixed(LocalDate.of(2026, 11, 5).atStartOfDay(seoul).toInstant(), seoul)

    private fun dday(
        id: String,
        title: String,
        date: LocalDate,
    ) = Item(
        id = id,
        content =
            ItemContent(
                type = ItemType.DDAY,
                title = title,
                colorKey = "PINK",
                iconKey = "STAR",
                date = date,
                countMode = CountMode.DDAY,
            ),
        ownerUserId = "user-me",
        updatedBy = "user-me",
        createdAt = 0L,
        updatedAt = 0L,
    )

    private val discharge = dday("discharge", "전역일", LocalDate.of(2027, 3, 19))

    /** 가장 가까운 디데이(자동 선택). 커버 사진이 있다 */
    private val toeic =
        dday("toeic", "토익 시험", LocalDate.of(2026, 11, 8)).let {
            it.copy(content = it.content.copy(backgroundImageId = "toeic-bg"))
        }
    private val configs = FakeWidgetConfigRepository()
    private val images = FakeImageRepository()
    private val profiles = FakeProfileRepository(me = UserProfile("user-me", "현우", null, profileImageId = "face"))

    private fun viewModel(saved: WidgetConfig? = null): WidgetConfigViewModel {
        saved?.let { kotlinx.coroutines.runBlocking { configs.saveConfig(it) } }
        return WidgetConfigViewModel(
            target = WidgetTarget(widgetId = 7, size = WidgetSize.MEDIUM),
            itemRepository = FakeItemRepository(listOf(discharge, toeic)),
            widgetConfigRepository = configs,
            settingsRepository = FakeSettingsRepository(ThemeSetting(AppThemeType.NAVY, AppThemeMode.SYSTEM)),
            photos = WidgetPhotos(images, profiles),
            clock = clock,
        )
    }

    @Test
    fun `처음 열면 자동 선택이고 가장 가까운 디데이로 미리보기`() {
        val state = viewModel().state.value

        assertFalse(state.isLoading)
        assertNull(state.selectedItemId)
        assertEquals(AppThemeType.NAVY, state.theme)
        assertEquals(listOf(toeic, discharge), state.choices.map { it.item })
        assertEquals(toeic, (state.display as WidgetDisplay.Dday).entry.item)
    }

    @Test
    fun `저장된 설정을 불러옴`() {
        val state =
            viewModel(
                WidgetConfig(7, WidgetSize.MEDIUM, itemId = "discharge", background = WidgetBackground.DARK),
            ).state.value

        assertEquals("discharge", state.selectedItemId)
        assertEquals(WidgetBackground.DARK, state.background)
    }

    @Test
    fun `삭제된 디데이를 고른 상태였으면 자동으로 되돌림`() {
        assertNull(viewModel(WidgetConfig(7, WidgetSize.MEDIUM, itemId = "deleted")).state.value.selectedItemId)
    }

    @Test
    fun `디데이를 고르면 미리보기가 바뀌고 저장하면 닫힘`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onAction(WidgetConfigAction.ItemSelected(discharge.id))
            viewModel.onAction(WidgetConfigAction.BackgroundSelected(WidgetBackground.WHITE))
            assertEquals(discharge, (viewModel.state.value.display as WidgetDisplay.Dday).entry.item)

            viewModel.event.test {
                viewModel.onAction(WidgetConfigAction.SaveClicked)
                assertEquals(WidgetConfigEvent.Close, awaitItem())
            }

            assertEquals(
                WidgetConfig(7, WidgetSize.MEDIUM, itemId = discharge.id, background = WidgetBackground.WHITE),
                configs.getConfig(7),
            )
        }

    @Test
    fun `닫기는 저장하지 않고 닫힘`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onAction(WidgetConfigAction.ItemSelected(discharge.id))

            viewModel.event.test {
                viewModel.onAction(WidgetConfigAction.CloseClicked)
                assertEquals(WidgetConfigEvent.Close, awaitItem())
            }

            assertNull(configs.getConfig(7))
        }

    @Test
    fun `사진 배경 · 프로필 표시를 저장하고 미리보기에 디데이 사진과 내 프로필이 보임`() =
        runTest {
            val bg = images.add("toeic-bg")
            val face = images.add("face")
            val viewModel = viewModel()

            viewModel.onAction(WidgetConfigAction.BackgroundSelected(WidgetBackground.PHOTO))
            viewModel.onAction(WidgetConfigAction.ShowProfileChanged(true))

            val state = viewModel.state.value
            assertEquals(bg, state.photo)
            assertEquals(listOf(face), state.profiles.map { it.photo })

            viewModel.event.test {
                viewModel.onAction(WidgetConfigAction.SaveClicked)
                awaitItem()
            }
            val saved = configs.getConfig(7)
            assertEquals(WidgetBackground.PHOTO, saved?.background)
            assertEquals(true, saved?.showProfile)
        }
}
