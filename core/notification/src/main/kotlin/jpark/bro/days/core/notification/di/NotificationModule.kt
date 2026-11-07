package jpark.bro.days.core.notification.di

import jpark.bro.days.core.notification.ReminderNotifier
import jpark.bro.days.core.notification.ReminderScheduler
import jpark.bro.days.core.notification.TodayRemindersCollector
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** 기념일 · 일정 로컬 알림. 상대 활동 알림은 이후 (module-map 9장 7단계) */
val notificationModule =
    module {
        single { ReminderNotifier(androidContext()) }
        // CoroutineScope: 앱 프로세스와 수명이 같은 범위 (widgetModule이 등록)
        single { ReminderScheduler(androidContext(), get(), get(), get(), get()) }
        factory { TodayRemindersCollector(get(), get(), get(), get(), get(), get()) }
    }
