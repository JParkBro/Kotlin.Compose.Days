package jpark.bro.days.core.testing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import jpark.bro.days.core.datastore.AppUpdateDataStore
import jpark.bro.days.core.datastore.SettingsDataStore
import java.io.File
import java.nio.file.Files

/** 테스트마다 새 임시 파일을 쓰는 DataStore */
fun testSettingsDataStore(): SettingsDataStore = SettingsDataStore(tempPreferences())

/** 테스트마다 새 임시 파일을 쓰는 인앱 업데이트 기록 */
fun testAppUpdateDataStore(): AppUpdateDataStore = AppUpdateDataStore(tempPreferences())

private fun tempPreferences(): DataStore<Preferences> {
    val dir = Files.createTempDirectory("settings").toFile()
    return PreferenceDataStoreFactory.create { File(dir, "settings.preferences_pb") }
}
