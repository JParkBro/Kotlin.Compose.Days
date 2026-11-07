package jpark.bro.days.core.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import jpark.bro.days.core.model.ItemReminder
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime

/**
 * 기념일 · 일정 알림 예약 (product-spec 4-5: 오전 9시 고정).
 * 매일 9시에 도는 WorkManager 1회 작업을 예약하고, 실행되면 다음 날 9시로 다시 예약한다.
 * 정확한 알람 권한을 쓰지 않아 기기 상태에 따라 몇 분 늦게 올 수 있다. 서버를 쓰지 않는다.
 */
class ReminderScheduler(
    private val context: Context,
    private val notifier: ReminderNotifier,
    private val clock: Clock,
) {
    /** 앱 시작 시 한 번 (Application). 이미 예약돼 있으면 그대로 둔다 */
    fun start() {
        notifier.createChannels()
        enqueue(ExistingWorkPolicy.KEEP)
    }

    /** 알림을 보낸 뒤 · 기기 시각이나 시간대가 바뀌었을 때 다음 9시로 다시 잡는다 */
    fun scheduleNext() {
        enqueue(ExistingWorkPolicy.REPLACE)
    }

    private fun enqueue(policy: ExistingWorkPolicy) {
        val request =
            OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayUntilNextReminder(ZonedDateTime.now(clock)))
                .build()
        WorkManager.getInstance(context).enqueueUniqueWork(REMINDER_WORK, policy, request)
    }

    private companion object {
        const val REMINDER_WORK = "daily_reminder"
    }
}

/** 다음 오전 9시(그 지역 시간 기준)까지 남은 시간. 9시 정각이 지났으면 다음 날 9시. 서머타임으로 하루가 23 · 25시간이어도 맞게 센다 */
internal fun delayUntilNextReminder(now: ZonedDateTime): Duration {
    val today = now.toLocalDate()
    val todayAt = today.atTime(ItemReminder.SendTime).atZone(now.zone)
    val next = if (now.isBefore(todayAt)) todayAt else today.plusDays(1).atTime(ItemReminder.SendTime).atZone(now.zone)
    return Duration.between(now, next)
}
