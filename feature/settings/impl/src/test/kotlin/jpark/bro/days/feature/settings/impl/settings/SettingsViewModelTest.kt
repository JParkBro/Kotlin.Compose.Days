package jpark.bro.days.feature.settings.impl.settings

import app.cash.turbine.test
import jpark.bro.days.core.model.AppThemeMode
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.ThemeSetting
import jpark.bro.days.core.testing.FakeSettingsRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSettingsRepository(ThemeSetting(AppThemeType.NAVY, AppThemeMode.DARK))

    // MainDispatcherRule이 적용된 뒤 만들어야 viewModelScope가 테스트 디스패처를 쓴다
    private val viewModel by lazy { SettingsViewModel(repository) }

    @Test
    fun `저장된 테마가 State에 들어간다`() {
        assertEquals(ThemeSetting(AppThemeType.NAVY, AppThemeMode.DARK), viewModel.state.value.themeSetting)
    }

    @Test
    fun `테마 시트를 열면 현재 설정이 선택된 상태로 열린다`() {
        viewModel.onAction(SettingsAction.ThemeClicked)

        assertEquals(ThemeSheetState(AppThemeType.NAVY, AppThemeMode.DARK), viewModel.state.value.themeSheet)
    }

    @Test
    fun `테마 · 모드를 고르고 적용하면 저장되고 시트가 닫힌다`() =
        runTest {
            viewModel.onAction(SettingsAction.ThemeClicked)
            viewModel.onAction(SettingsAction.ThemeTypeSelected(AppThemeType.SAGE))
            viewModel.onAction(SettingsAction.ThemeModeSelected(AppThemeMode.LIGHT))

            viewModel.onAction(SettingsAction.ThemeApplyClicked)

            assertEquals(ThemeSetting(AppThemeType.SAGE, AppThemeMode.LIGHT), repository.observeThemeSetting().first())
            assertNull(viewModel.state.value.themeSheet)
            assertEquals(ThemeSetting(AppThemeType.SAGE, AppThemeMode.LIGHT), viewModel.state.value.themeSetting)
        }

    @Test
    fun `적용하지 않고 닫으면 저장하지 않는다`() =
        runTest {
            viewModel.onAction(SettingsAction.ThemeClicked)
            viewModel.onAction(SettingsAction.ThemeTypeSelected(AppThemeType.LAVENDER))

            viewModel.onAction(SettingsAction.ThemeSheetDismissed)

            assertNull(viewModel.state.value.themeSheet)
            assertEquals(ThemeSetting(AppThemeType.NAVY, AppThemeMode.DARK), repository.observeThemeSetting().first())
        }

    @Test
    fun `저장에 실패하면 시트가 열린 채 실패 Event를 보낸다`() =
        runTest {
            repository.failNextSave = true
            viewModel.onAction(SettingsAction.ThemeClicked)
            viewModel.onAction(SettingsAction.ThemeTypeSelected(AppThemeType.SAGE))

            viewModel.event.test {
                viewModel.onAction(SettingsAction.ThemeApplyClicked)

                assertEquals(SettingsEvent.ThemeSaveFailed, awaitItem())
            }
            assertEquals(
                ThemeSheetState(AppThemeType.SAGE, AppThemeMode.DARK, isSaving = false),
                viewModel.state.value.themeSheet,
            )
        }

    @Test
    fun `솔로일 때 커플 메뉴는 커플 코드 연결로 이동한다`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(SettingsAction.MenuClicked(SettingsDestination.COUPLE))

                assertEquals(SettingsEvent.Navigate(SettingsDestination.PAIRING), awaitItem())
            }
        }

    @Test
    fun `메뉴를 누르면 해당 화면으로 이동한다`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(SettingsAction.MenuClicked(SettingsDestination.POLICY))

                assertEquals(SettingsEvent.Navigate(SettingsDestination.POLICY), awaitItem())
            }
        }
}
