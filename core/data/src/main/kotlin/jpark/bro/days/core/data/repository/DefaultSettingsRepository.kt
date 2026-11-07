package jpark.bro.days.core.data.repository

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.datastore.SettingsDataStore
import jpark.bro.days.core.model.NotificationSettings
import jpark.bro.days.core.model.ThemeSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

internal class DefaultSettingsRepository(
    private val settingsDataStore: SettingsDataStore,
) : SettingsRepository {
    /** 메모리에만 두는 미리보기 값 (앱을 다시 켜면 사라짐) */
    private val themePreview = MutableStateFlow<ThemeSetting?>(null)

    override fun observeThemeSetting(): Flow<ThemeSetting> = settingsDataStore.themeSetting

    override fun observeEffectiveThemeSetting(): Flow<ThemeSetting> =
        combine(settingsDataStore.themeSetting, themePreview) { saved, preview -> preview ?: saved }

    override suspend fun setThemeSetting(setting: ThemeSetting): Result<Unit> =
        suspendRunCatching { settingsDataStore.setThemeSetting(setting) }

    override fun setThemePreview(setting: ThemeSetting?) {
        themePreview.value = setting
    }

    override fun observeNotificationSettings(): Flow<NotificationSettings> = settingsDataStore.notificationSettings

    override suspend fun setNotificationSettings(settings: NotificationSettings): Result<Unit> =
        suspendRunCatching { settingsDataStore.setNotificationSettings(settings) }

    override fun observeCoupleBannerDismissed(): Flow<Boolean> = settingsDataStore.coupleBannerDismissed

    override suspend fun dismissCoupleBanner(): Result<Unit> =
        suspendRunCatching {
            settingsDataStore.setCoupleBannerDismissed()
        }
}
