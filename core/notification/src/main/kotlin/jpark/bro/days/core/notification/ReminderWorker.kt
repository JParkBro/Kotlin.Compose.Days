package jpark.bro.days.core.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.repository.ActivityRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** 매일 알림 시각(기본 오전 9시): 오늘 알림을 띄우고 다음 날을 예약한다. Worker는 Android 진입점이라 Koin에서 직접 꺼낸다 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params),
    KoinComponent {
    private val collector: TodayRemindersCollector by inject()
    private val notifier: ReminderNotifier by inject()
    private val activities: ActivityRepository by inject()
    private val scheduler: ReminderScheduler by inject()

    override suspend fun doWork(): Result {
        // 오늘 것을 못 보내도 다음 날 예약은 잡는다
        val delivered =
            suspendRunCatching {
                val reminders = collector.collect()
                notifier.notifyAll(reminders)
                // 알림 권한이 없어 띄우지 못했어도 알림 목록에는 남긴다
                activities.record(reminders.toActivityContents())
            }
        scheduler.scheduleNext()
        return if (delivered.isSuccess) Result.success() else Result.failure()
    }
}
