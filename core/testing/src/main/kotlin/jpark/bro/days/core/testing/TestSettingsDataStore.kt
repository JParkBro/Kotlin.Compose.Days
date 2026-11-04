package jpark.bro.days.core.testing

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import jpark.bro.days.core.datastore.SettingsDataStore
import java.io.File
import java.nio.file.Files

/** 테스트마다 새 임시 파일을 쓰는 DataStore */
fun testSettingsDataStore(): SettingsDataStore {
    val dir = Files.createTempDirectory("settings").toFile()
    return SettingsDataStore(PreferenceDataStoreFactory.create { File(dir, "settings.preferences_pb") })
}
