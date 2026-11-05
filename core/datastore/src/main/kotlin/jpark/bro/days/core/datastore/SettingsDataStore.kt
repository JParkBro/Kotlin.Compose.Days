package jpark.bro.days.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import jpark.bro.days.core.model.ThemeSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    /** 홈의 "커플 코드로 연결하기" 카드를 닫았는지 (product-spec 4-1) */
    val coupleBannerDismissed: Flow<Boolean> = dataStore.data.map { prefs -> prefs[COUPLE_BANNER_DISMISSED] ?: false }

    suspend fun setCoupleBannerDismissed() {
        dataStore.edit { prefs -> prefs[COUPLE_BANNER_DISMISSED] = true }
    }

    /** 내가 마지막으로 발급한 커플 코드. 24시간 안에 다시 열면 같은 코드를 보여주고, 연결되거나 새로 발급하면 지운다 */
    suspend fun issuedPairCode(): IssuedPairCode? =
        dataStore.data.first()[ISSUED_PAIR_CODE]?.let(IssuedPairCode::decode)

    suspend fun setIssuedPairCode(code: IssuedPairCode?) {
        dataStore.edit { prefs ->
            if (code == null) prefs.remove(ISSUED_PAIR_CODE) else prefs[ISSUED_PAIR_CODE] = code.encode()
        }
    }

    /** 온보딩 소개를 이 기기에서 본 적 있는지. 본 뒤로는 로그인부터 */
    suspend fun introSeen(): Boolean = dataStore.data.first()[INTRO_SEEN] ?: false

    suspend fun setIntroSeen() {
        dataStore.edit { prefs -> prefs[INTRO_SEEN] = true }
    }

    /** 이 기기에서 약관 동의를 마친(또는 서버에서 확인한) 계정. 한 기기에 여러 계정이 로그인할 수 있다 */
    suspend fun termsAgreed(userId: String): Boolean = userId in dataStore.data.first()[TERMS_AGREED_USERS].orEmpty()

    suspend fun setTermsAgreed(userId: String) {
        dataStore.edit { prefs -> prefs[TERMS_AGREED_USERS] = prefs[TERMS_AGREED_USERS].orEmpty() + userId }
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
        val COUPLE_BANNER_DISMISSED = booleanPreferencesKey("couple_banner_dismissed")
        val ISSUED_PAIR_CODE = stringPreferencesKey("issued_pair_code")
        val INTRO_SEEN = booleanPreferencesKey("intro_seen")
        val TERMS_AGREED_USERS = stringSetPreferencesKey("terms_agreed_users")
    }
}

private inline fun <reified E : Enum<E>> String?.toEnumOr(default: E): E =
    enumValues<E>().firstOrNull { it.name == this } ?: default

/** 발급한 계정 · 코드 · 만료 시각 (한 기기에 여러 계정이 로그인할 수 있어 계정을 함께 둔다) */
data class IssuedPairCode(
    val userId: String,
    val code: String,
    val expiresAt: Long,
) {
    internal fun encode(): String = "$userId$SEPARATOR$code$SEPARATOR$expiresAt"

    internal companion object {
        private const val SEPARATOR = "|"
        private const val PARTS = 3

        fun decode(value: String): IssuedPairCode? {
            val parts = value.split(SEPARATOR)
            val expiresAt = parts.getOrNull(2)?.toLongOrNull()
            return if (parts.size == PARTS && expiresAt != null) IssuedPairCode(parts[0], parts[1], expiresAt) else null
        }
    }
}
