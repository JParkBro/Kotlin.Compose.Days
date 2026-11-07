package jpark.bro.days.core.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import jpark.bro.days.core.common.suspendRunCatching
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** 매일 오전 9시: 오늘 알림을 띄우고 다음 날 9시를 예약한다. Worker는 Android 진입점이라 Koin에서 직접 꺼낸다 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params),
    KoinComponent {
    private val delivery: ReminderDelivery by inject()
    private val scheduler: ReminderScheduler by inject()

    override suspend fun doWork(): Result {
        // 오늘 것을 못 보내도 다음 날 예약은 잡는다
        val delivered = suspendRunCatching { delivery.deliverToday() }
        scheduler.scheduleNext()
        return if (delivered.isSuccess) Result.success() else Result.failure()
    }
}
