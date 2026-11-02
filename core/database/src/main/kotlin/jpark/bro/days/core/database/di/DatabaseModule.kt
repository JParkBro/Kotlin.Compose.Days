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
    }
