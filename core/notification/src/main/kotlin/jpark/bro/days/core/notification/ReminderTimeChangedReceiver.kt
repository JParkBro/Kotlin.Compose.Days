package jpark.bro.days.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** 기기 시각 · 시간대가 바뀌면 그 지역의 다음 오전 9시로 다시 예약한다 */
class ReminderTimeChangedReceiver :
    BroadcastReceiver(),
    KoinComponent {
    private val scheduler: ReminderScheduler by inject()

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action !in TimeActions) return
        scheduler.scheduleNext()
    }

    private companion object {
        val TimeActions = setOf(Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)
    }
}
