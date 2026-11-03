package jpark.bro.days.feature.settings.impl.settings

import app.cash.turbine.test
import jpark.bro.days.core.model.AppThemeMode
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.AuthProvider
import jpark.bro.days.core.model.AuthUser
import jpark.bro.days.core.model.ThemeSetting
import jpark.bro.days.core.testing.FakeAuthRepository
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

    private val saved = ThemeSetting(AppThemeType.NAVY, AppThemeMode.DARK)
    private val repository = FakeSettingsRepository(saved)

    // MainDispatcherRule이 적용된 뒤 만들어야 viewModelScope가 테스트 디스패처를 쓴다
    private val authRepository =
        FakeAuthRepository(AuthUser("google-1", AuthProvider.GOOGLE, email = "hyunwoo@example.com"))
    private val viewModel by lazy { SettingsViewModel(repository, authRepository) }

    @Test
    fun `저장된 테마가 State에 들어간다`() {
        assertEquals(saved, viewModel.state.value.themeSetting)
    }

    @Test
    fun `테마 시트를 열면 현재 설정이 선택된 상태로 열리고 미리보기는 아직 없다`() {
        viewModel.onAction(SettingsAction.ThemeClicked)

        assertEquals(ThemeSheetState(AppThemeType.NAVY, AppThemeMode.DARK), viewModel.state.value.themeSheet)
        assertNull(repository.themePreview)
    }

    @Test
    fun `테마 · 모드를 고르면 저장하지 않고 앱에 바로 미리보기로 적용된다`() =
        runTest {
            viewModel.onAction(SettingsAction.ThemeClicked)

            viewModel.onAction(SettingsAction.ThemeTypeSelected(AppThemeType.LAVENDER))
            viewModel.onAction(SettingsAction.ThemeModeSelected(AppThemeMode.LIGHT))

            val preview = ThemeSetting(AppThemeType.LAVENDER, AppThemeMode.LIGHT)
            assertEquals(preview, repository.observeEffectiveThemeSetting().first())
            assertEquals(saved, repository.observeThemeSetting().first())
        }

    @Test
    fun `적용하면 저장되고 미리보기가 끝나며 시트가 닫힌다`() =
        runTest {
            viewModel.onAction(SettingsAction.ThemeClicked)
            viewModel.onAction(SettingsAction.ThemeTypeSelected(AppThemeType.SAGE))
            viewModel.onAction(SettingsAction.ThemeModeSelected(AppThemeMode.LIGHT))

            viewModel.onAction(SettingsAction.ThemeApplyClicked)

            val applied = ThemeSetting(AppThemeType.SAGE, AppThemeMode.LIGHT)
            assertEquals(applied, repository.observeThemeSetting().first())
            assertEquals(applied, repository.observeEffectiveThemeSetting().first())
            assertNull(repository.themePreview)
            assertNull(viewModel.state.value.themeSheet)
            assertEquals(applied, viewModel.state.value.themeSetting)
        }

    @Test
    fun `적용하지 않고 닫으면 저장하지 않고 원래 테마로 돌아간다`() =
        runTest {
            viewModel.onAction(SettingsAction.ThemeClicked)
            viewModel.onAction(SettingsAction.ThemeTypeSelected(AppThemeType.LAVENDER))

            viewModel.onAction(SettingsAction.ThemeSheetDismissed)

            assertNull(viewModel.state.value.themeSheet)
            assertNull(repository.themePreview)
            assertEquals(saved, repository.observeThemeSetting().first())
            assertEquals(saved, repository.observeEffectiveThemeSetting().first())
        }

    @Test
    fun `저장에 실패하면 시트와 미리보기가 유지되고 실패 Event를 보낸다`() =
        runTest {
            repository.failNextSave = true
            viewModel.onAction(SettingsAction.ThemeClicked)
            viewModel.onAction(SettingsAction.ThemeTypeSelected(AppThemeType.SAGE))

            viewModel.event.test {
                viewModel.onAction(SettingsAction.ThemeApplyClicked)

                assertEquals(SettingsEvent.ThemeSaveFailed, awaitItem())
            }
            val selected = ThemeSheetState(AppThemeType.SAGE, AppThemeMode.DARK, isSaving = false)
            assertEquals(selected, viewModel.state.value.themeSheet)
            assertEquals(ThemeSetting(AppThemeType.SAGE, AppThemeMode.DARK), repository.themePreview)
            assertEquals(saved, repository.observeThemeSetting().first())
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

    @Test
    fun `로그인한 계정 이메일을 계정 메뉴에 보여줌`() {
        assertEquals("hyunwoo@example.com", viewModel.state.value.accountEmail)
    }
}
