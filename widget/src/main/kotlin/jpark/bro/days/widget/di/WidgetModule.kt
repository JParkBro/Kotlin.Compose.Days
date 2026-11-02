package jpark.bro.days.widget.di

import jpark.bro.days.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val widgetModule =
    module {
        // 위젯 갱신 · 리시버 비동기 작업용. 앱 프로세스와 수명이 같다
        single<CoroutineScope> { CoroutineScope(SupervisorJob()) }
        single { WidgetUpdater(androidContext(), get(), get(), get(), get(), get()) }
    }
