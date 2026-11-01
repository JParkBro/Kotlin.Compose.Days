package jpark.bro.days

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.model.ThemeSetting
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5_000L

/** 앱 전체 테마 (설정 > 테마에서 저장한 값, 테마 시트에서 고르는 중이면 미리보기 값) */
internal class MainViewModel(
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val themeSetting: StateFlow<ThemeSetting> =
        settingsRepository
            .observeEffectiveThemeSetting()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ThemeSetting())
}
