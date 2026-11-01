package jpark.bro.days.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "coupleDate")

class CoupleDateDataStore(
    private val context: Context,
) {
    companion object {
        private val COUPLE_DATE_KEY = longPreferencesKey("couple_date")
    }

    suspend fun saveCoupleDate(date: Long) {
        context.dataStore.edit { preferences ->
            preferences[COUPLE_DATE_KEY] = date
        }
    }

    val coupleDate: Flow<Long?> =
        context.dataStore.data.map { preferences ->
            preferences[COUPLE_DATE_KEY]
        }
}
