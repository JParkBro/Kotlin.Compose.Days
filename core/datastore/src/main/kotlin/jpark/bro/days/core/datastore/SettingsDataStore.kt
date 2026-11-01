package jpark.bro.days.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import jpark.bro.days.core.model.ThemeSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 기기에만 저장하는 앱 설정 (서버로 보내지 않음) */
class SettingsDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    val themeSetting: Flow<ThemeSetting> =
        dataStore.data.map { prefs ->
            val default = ThemeSetting()
            ThemeSetting(
                type = prefs[THEME_TYPE].toEnumOr(default.type),
                mode = prefs[THEME_MODE].toEnumOr(default.mode),
            )
        }

    suspend fun setThemeSetting(setting: ThemeSetting) {
        dataStore.edit { prefs ->
            prefs[THEME_TYPE] = setting.type.name
            prefs[THEME_MODE] = setting.mode.name
        }
    }

    private companion object {
        val THEME_TYPE = stringPreferencesKey("theme_type")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}

private inline fun <reified E : Enum<E>> String?.toEnumOr(default: E): E =
    enumValues<E>().firstOrNull { it.name == this } ?: default
