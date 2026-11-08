package jpark.bro.days.core.notification.di

import jpark.bro.days.core.data.repository.ActivityNotifier
import jpark.bro.days.core.notification.PartnerActivityNotifier
import jpark.bro.days.core.notification.ReminderNotifier
import jpark.bro.days.core.notification.ReminderScheduler
import jpark.bro.days.core.notification.TodayRemindersCollector
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** 기념일 · 일정 로컬 알림, 상대 활동 알림 (core:data ActivityNotifier 구현) */
val notificationModule =
    module {
        single { ReminderNotifier(androidContext()) }
        // CoroutineScope: 앱 프로세스와 수명이 같은 범위 (widgetModule이 등록)
        single { ReminderScheduler(androidContext(), get(), get(), get(), get()) }
        factory { TodayRemindersCollector(get(), get(), get(), get(), get(), get()) }
        single<ActivityNotifier> { PartnerActivityNotifier(androidContext(), get(), get(), get()) }
    }
