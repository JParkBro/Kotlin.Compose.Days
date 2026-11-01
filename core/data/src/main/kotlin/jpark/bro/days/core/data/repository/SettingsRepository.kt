package jpark.bro.days.core.data.repository

import jpark.bro.days.core.model.ThemeSetting
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeThemeSetting(): Flow<ThemeSetting>

    suspend fun setThemeSetting(setting: ThemeSetting): Result<Unit>
}
