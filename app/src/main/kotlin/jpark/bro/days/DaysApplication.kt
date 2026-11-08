package jpark.bro.days

import android.app.Application
import jpark.bro.days.core.data.auth.KakaoSetup
import jpark.bro.days.core.firebase.FirebaseSetup
import jpark.bro.days.core.notification.ReminderScheduler
import jpark.bro.days.core.sync.SyncManager
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
        // Firebase 인스턴스를 쓰기 전에 App Check · 캐시 · 에뮬레이터를 설정한다
        FirebaseSetup.initialize(emulatorHost = BuildConfig.FIREBASE_EMULATOR_HOST)
        KakaoSetup.initialize(this, nativeAppKey = BuildConfig.KAKAO_NATIVE_APP_KEY)
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@DaysApplication)
            modules(appModule)
        }
        // 항목 · 테마가 바뀌면 홈 화면 위젯을 다시 그린다 (module-map 10장)
        get<WidgetUpdater>().start()
        // 공유 항목 · 커플 동기화 (로그인 + 포그라운드일 때 리스너, 변경이 생기면 Push 예약)
        get<SyncManager>().start()
        // 디데이 · 일정 · 자동 기념일 알림: 매일 오전 9시 (product-spec 4-5)
        get<ReminderScheduler>().start()
    }
}
