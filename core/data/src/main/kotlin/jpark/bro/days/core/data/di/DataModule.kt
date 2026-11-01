package jpark.bro.days.core.data.di

import jpark.bro.days.core.data.repository.DefaultSettingsRepository
import jpark.bro.days.core.data.repository.SettingsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule =
    module {
        singleOf(::DefaultSettingsRepository) bind SettingsRepository::class
    }
