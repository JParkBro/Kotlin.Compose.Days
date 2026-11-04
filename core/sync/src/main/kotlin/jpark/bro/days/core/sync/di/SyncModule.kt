package jpark.bro.days.core.sync.di

import jpark.bro.days.core.sync.SyncEngine
import jpark.bro.days.core.sync.SyncManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val syncModule =
    module {
        single { SyncEngine(get(), get(), get(), get(), get()) }
        single { SyncManager(androidContext(), get(), get(), get()) }
    }
