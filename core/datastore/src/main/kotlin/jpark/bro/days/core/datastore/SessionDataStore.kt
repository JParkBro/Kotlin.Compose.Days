package jpark.bro.days.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * 로그인 전 임시 사용자 id (기기에만 저장).
 * 로그인(Firebase Auth)을 만들면 uid로 바뀌고 이 저장소는 제거한다.
 */
class SessionDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    /** 저장된 id를 돌려주고, 없으면 [newId]로 만들어 저장한다. edit 안에서 처리해 동시에 불려도 하나만 만든다 */
    suspend fun getOrCreateLocalUserId(newId: () -> String): String {
        var userId = ""
        dataStore.edit { prefs ->
            userId = prefs[LOCAL_USER_ID] ?: newId().also { prefs[LOCAL_USER_ID] = it }
        }
        return userId
    }

    private companion object {
        val LOCAL_USER_ID = stringPreferencesKey("local_user_id")
    }
}
