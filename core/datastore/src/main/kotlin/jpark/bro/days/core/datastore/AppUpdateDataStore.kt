package jpark.bro.days.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** 인앱 업데이트 안내 기록 (기기에만 저장, [SettingsDataStore]와 같은 파일) */
class AppUpdateDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    /** "업데이트 안내"에서 마지막으로 "나중에"를 누른 날 (yyyy-MM-dd). 없거나 형식이 깨졌으면 null */
    suspend fun promptDismissedDate(): LocalDate? =
        dataStore.data.first()[PROMPT_DISMISSED_DATE]?.let { value ->
            try {
                LocalDate.parse(value)
            } catch (_: DateTimeParseException) {
                null
            }
        }

    suspend fun setPromptDismissedDate(date: LocalDate) {
        dataStore.edit { prefs -> prefs[PROMPT_DISMISSED_DATE] = date.toString() }
    }

    private companion object {
        val PROMPT_DISMISSED_DATE = stringPreferencesKey("update_prompt_dismissed_date")
    }
}
