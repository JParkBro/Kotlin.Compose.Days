package jpark.bro.days.core.notification.di

import jpark.bro.days.core.notification.ReminderDelivery
import jpark.bro.days.core.notification.ReminderNotifier
import jpark.bro.days.core.notification.ReminderScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** 기념일 · 일정 로컬 알림. 상대 활동 알림은 이후 (module-map 9장 7단계) */
val notificationModule =
    module {
        single { ReminderNotifier(androidContext()) }
        single { ReminderScheduler(androidContext(), get(), get()) }
        factory { ReminderDelivery(get(), get(), get(), get(), get(), get()) }
    }
