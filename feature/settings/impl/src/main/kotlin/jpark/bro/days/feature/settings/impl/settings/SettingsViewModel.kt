package jpark.bro.days.feature.settings.impl.settings

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.AuthRepository
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.model.ThemeSetting
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    authRepository: AuthRepository,
) : MviViewModel<SettingsState, SettingsAction, SettingsEvent>(SettingsState()) {
    init {
        authRepository
            .observeUser()
            .onEach { user -> setState { copy(accountEmail = user?.email) } }
            .launchIn(viewModelScope)
        settingsRepository
            .observeThemeSetting()
            .onEach { setting -> setState { copy(themeSetting = setting) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.MenuClicked -> sendEvent(SettingsEvent.Navigate(resolve(action.destination)))
            is SettingsAction.SupportClicked -> sendEvent(SettingsEvent.OpenSupport(action.item))
            SettingsAction.ThemeClicked -> openThemeSheet()
            SettingsAction.ThemeSheetDismissed -> closeThemeSheet()
            is SettingsAction.ThemeTypeSelected -> updateSheet { copy(type = action.type) }
            is SettingsAction.ThemeModeSelected -> updateSheet { copy(mode = action.mode) }
            SettingsAction.ThemeApplyClicked -> applyTheme()
        }
    }

    /** 솔로일 때 커플 메뉴는 커플 코드 연결로 보낸다 (product-spec 6장) */
    private fun resolve(destination: SettingsDestination): SettingsDestination =
        if (destination == SettingsDestination.COUPLE && currentState.partnerName == null) {
            SettingsDestination.PAIRING
        } else {
            destination
        }

    private fun openThemeSheet() {
        val saved = currentState.themeSetting
        setState { copy(themeSheet = ThemeSheetState(saved.type, saved.mode)) }
    }

    /** 시트에서 고른 값을 바로 앱 전체에 미리보기로 보여준다. 저장은 적용하기에서만 한다. */
    private fun updateSheet(reduce: ThemeSheetState.() -> ThemeSheetState) {
        val sheet = currentState.themeSheet?.reduce() ?: return
        setState { copy(themeSheet = sheet) }
        settingsRepository.setThemePreview(ThemeSetting(sheet.type, sheet.mode))
    }

    /** 적용하지 않고 닫으면(X · 바깥 · 아래로 내리기) 미리보기를 끝내 저장된 테마로 되돌린다 */
    private fun closeThemeSheet() {
        settingsRepository.setThemePreview(null)
        setState { copy(themeSheet = null) }
    }

    private fun applyTheme() {
        val sheet = currentState.themeSheet ?: return
        if (sheet.isSaving) return
        setState { copy(themeSheet = sheet.copy(isSaving = true)) }
        viewModelScope.launch {
            settingsRepository
                .setThemeSetting(ThemeSetting(sheet.type, sheet.mode))
                .onSuccess { closeThemeSheet() }
                .onFailure {
                    // 실패하면 미리보기 · 시트를 그대로 두고 다시 시도할 수 있게 한다
                    setState { copy(themeSheet = themeSheet?.copy(isSaving = false)) }
                    sendEvent(SettingsEvent.ThemeSaveFailed)
                }
        }
    }

    /** 시트를 연 채 화면을 떠나면 미리보기가 남지 않게 한다 */
    override fun onCleared() {
        if (currentState.themeSheet != null) settingsRepository.setThemePreview(null)
        super.onCleared()
    }
}
