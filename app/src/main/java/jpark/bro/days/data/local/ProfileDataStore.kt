package jpark.bro.days.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "profile")

class ProfileDataStore (
    private val context: Context
) {
    companion object {
        val A_NAME_KEY = stringPreferencesKey("a_name")
        val A_BIRTHDAY_KEY = longPreferencesKey("a_birthday")
        val B_NAME_KEY = stringPreferencesKey("b_name")
        val B_BIRTHDAY_KEY = longPreferencesKey("b_birthday")
    }

    suspend fun saveName(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun saveBirthday(key: Preferences.Key<Long>, value: Long) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getName(key: Preferences.Key<String>): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[key]
        }
    }

    fun getBirthday(key: Preferences.Key<Long>): Flow<Long?> {
        return context.dataStore.data.map { preferences ->
            preferences[key]
        }
    }
}