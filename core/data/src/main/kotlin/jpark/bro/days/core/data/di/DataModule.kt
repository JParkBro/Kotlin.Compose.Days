package jpark.bro.days.core.data.di

import jpark.bro.days.core.common.IdGenerator
import jpark.bro.days.core.common.UuidIdGenerator
import jpark.bro.days.core.data.repository.DefaultItemRepository
import jpark.bro.days.core.data.repository.DefaultSettingsRepository
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.data.session.LocalUserSession
import jpark.bro.days.core.data.session.UserSession
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import java.time.Clock

val dataModule =
    module {
        single<Clock> { Clock.systemDefaultZone() }
        single<IdGenerator> { UuidIdGenerator }
        singleOf(::LocalUserSession) bind UserSession::class
        singleOf(::DefaultSettingsRepository) bind SettingsRepository::class
        singleOf(::DefaultItemRepository) bind ItemRepository::class
    }
