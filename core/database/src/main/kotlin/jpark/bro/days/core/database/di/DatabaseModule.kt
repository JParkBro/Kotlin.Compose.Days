package jpark.bro.days.core.database.di

import androidx.room.Room
import jpark.bro.days.core.database.DaysDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule =
    module {
        single {
            Room.databaseBuilder(androidContext(), DaysDatabase::class.java, "days.db").build()
        }
        single { get<DaysDatabase>().itemDao() }
        single { get<DaysDatabase>().userItemPrefDao() }
        single { get<DaysDatabase>().itemReminderDao() }
        single { get<DaysDatabase>().widgetConfigDao() }
        single { get<DaysDatabase>().coupleDao() }
        single { get<DaysDatabase>().anniversaryHideDao() }
        single { get<DaysDatabase>().syncCursorDao() }
        single { get<DaysDatabase>().itemSyncDao() }
        single { get<DaysDatabase>().userDao() }
        single { get<DaysDatabase>().coupleItemDao() }
        single { get<DaysDatabase>().imageDao() }
        single { get<DaysDatabase>().imageSyncDao() }
        single { get<DaysDatabase>().activityLogDao() }
        single { get<DaysDatabase>().accountDataDao() }
        single { get<DaysDatabase>().backupDao() }
    }
