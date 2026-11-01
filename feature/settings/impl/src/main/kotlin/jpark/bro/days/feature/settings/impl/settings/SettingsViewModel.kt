package jpark.bro.days.feature.settings.impl.settings

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.model.ThemeSetting
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : MviViewModel<SettingsState, SettingsAction, SettingsEvent>(SettingsState()) {
    init {
        settingsRepository
            .observeThemeSetting()
            .onEach { setting -> setState { copy(themeSetting = setting) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.MenuClicked -> {
                sendEvent(SettingsEvent.Navigate(resolve(action.destination)))
            }

            is SettingsAction.SupportClicked -> {
                sendEvent(SettingsEvent.OpenSupport(action.item))
            }

            SettingsAction.ThemeClicked -> {
                setState { copy(themeSheet = ThemeSheetState(themeSetting.type, themeSetting.mode)) }
            }

            SettingsAction.ThemeSheetDismissed -> {
                setState { copy(themeSheet = null) }
            }

            is SettingsAction.ThemeTypeSelected -> {
                updateSheet { copy(type = action.type) }
            }

            is SettingsAction.ThemeModeSelected -> {
                updateSheet { copy(mode = action.mode) }
            }

            SettingsAction.ThemeApplyClicked -> {
                applyTheme()
            }
        }
    }

    /** 솔로일 때 커플 메뉴는 커플 코드 연결로 보낸다 (product-spec 6장) */
    private fun resolve(destination: SettingsDestination): SettingsDestination =
        if (destination == SettingsDestination.COUPLE && currentState.partnerName == null) {
            SettingsDestination.PAIRING
        } else {
            destination
        }

    private fun updateSheet(reduce: ThemeSheetState.() -> ThemeSheetState) {
        setState { copy(themeSheet = themeSheet?.reduce()) }
    }

    private fun applyTheme() {
        val sheet = currentState.themeSheet ?: return
        if (sheet.isSaving) return
        updateSheet { copy(isSaving = true) }
        viewModelScope.launch {
            settingsRepository
                .setThemeSetting(ThemeSetting(sheet.type, sheet.mode))
                .onSuccess { setState { copy(themeSheet = null) } }
                .onFailure {
                    updateSheet { copy(isSaving = false) }
                    sendEvent(SettingsEvent.ThemeSaveFailed)
                }
        }
    }
}
