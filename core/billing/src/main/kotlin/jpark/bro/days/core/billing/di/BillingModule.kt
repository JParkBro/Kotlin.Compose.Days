package jpark.bro.days.core.billing.di

import jpark.bro.days.core.billing.PlaySupportStore
import jpark.bro.days.core.billing.SupportStore
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val billingModule =
    module {
        singleOf(::PlaySupportStore) bind SupportStore::class
    }
