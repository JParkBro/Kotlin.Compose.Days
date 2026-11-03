package jpark.bro.days.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import jpark.bro.days.core.datastore.SettingsDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val datastoreModule =
    module {
        // 테마 · 모드 · 커플 카드 닫음 (이후 광고 노출 기록, 온보딩 완료 여부)
        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.create { androidContext().preferencesDataStoreFile("settings") }
        }
        singleOf(::SettingsDataStore)
    }
