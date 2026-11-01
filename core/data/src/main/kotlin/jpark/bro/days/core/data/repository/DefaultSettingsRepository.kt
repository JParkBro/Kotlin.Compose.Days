package jpark.bro.days.core.data.repository

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.datastore.SettingsDataStore
import jpark.bro.days.core.model.ThemeSetting
import kotlinx.coroutines.flow.Flow

internal class DefaultSettingsRepository(
    private val settingsDataStore: SettingsDataStore,
) : SettingsRepository {
    override fun observeThemeSetting(): Flow<ThemeSetting> = settingsDataStore.themeSetting

    override suspend fun setThemeSetting(setting: ThemeSetting): Result<Unit> =
        suspendRunCatching { settingsDataStore.setThemeSetting(setting) }
}
