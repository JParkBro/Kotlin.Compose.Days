package jpark.bro.days.core.sync.di

import jpark.bro.days.core.firebase.config.RemoteFlags
import jpark.bro.days.core.sync.CoupleDataSync
import jpark.bro.days.core.sync.ImageSync
import jpark.bro.days.core.sync.ProfileSync
import jpark.bro.days.core.sync.SyncEngine
import jpark.bro.days.core.sync.SyncManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val syncModule =
    module {
        single { ProfileSync(get(), get(), get()) }
        single { ImageSync(get(), get(), get(), get(), get<RemoteFlags>().uploadEnabled) }
        single { CoupleDataSync(get(), get(), get(), get(), get()) }
        single { SyncEngine(get(), get(), get(), get(), get()) }
        single { SyncManager(androidContext(), get(), get(), get()) }
    }
