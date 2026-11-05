package jpark.bro.days.widget.di

import jpark.bro.days.core.data.repository.HomeWidgets
import jpark.bro.days.widget.DefaultHomeWidgets
import jpark.bro.days.widget.WidgetPhotos
import jpark.bro.days.widget.WidgetUpdater
import jpark.bro.days.widget.config.WidgetConfigViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val widgetModule =
    module {
        // 위젯 갱신 · 리시버 비동기 작업용. 앱 프로세스와 수명이 같다
        single<CoroutineScope> { CoroutineScope(SupervisorJob()) }
        single { WidgetUpdater(androidContext(), get(), get(), get(), get(), get()) }
        single<HomeWidgets> { DefaultHomeWidgets(androidContext(), get()) }
        factory { WidgetPhotos(get(), get()) }
        viewModel { params ->
            WidgetConfigViewModel(
                target = params.get(),
                itemRepository = get(),
                widgetConfigRepository = get(),
                settingsRepository = get(),
                photos = get(),
                clock = get(),
            )
        }
    }
