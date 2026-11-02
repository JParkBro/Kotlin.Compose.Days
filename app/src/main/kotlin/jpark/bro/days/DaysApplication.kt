package jpark.bro.days

import android.app.Application
import jpark.bro.days.di.appModule
import jpark.bro.days.widget.WidgetUpdater
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class DaysApplication :
    Application(),
    KoinComponent {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@DaysApplication)
            modules(appModule)
        }
        // 항목 · 테마가 바뀌면 홈 화면 위젯을 다시 그린다 (module-map 10장)
        get<WidgetUpdater>().start()
    }
}
