package jpark.bro.days.core.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import jpark.bro.days.core.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * 기념일 · 일정 알림 예약. 시각은 알림 설정에서 고른 시각(기본 오전 9시, product-spec 6장).
 * 매일 그 시각에 도는 WorkManager 1회 작업을 예약하고, 실행되면 다음 날로 다시 예약한다.
 * 정확한 알람 권한을 쓰지 않아 기기 상태에 따라 몇 분 늦게 올 수 있다. 서버를 쓰지 않는다.
 */
class ReminderScheduler(
    private val context: Context,
    private val notifier: ReminderNotifier,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
    private val scope: CoroutineScope,
) {
    /**
     * 앱 시작 시 한 번 (Application). 이미 예약돼 있으면 그대로 두고,
     * 앱이 떠 있는 동안 알림 시각을 바꾸면 바로 새 시각으로 다시 잡는다
     */
    fun start() {
        notifier.createChannels()
        scope.launch {
            var policy = ExistingWorkPolicy.KEEP
            settingsRepository
                .observeNotificationSettings()
                .map { it.sendTime }
                .distinctUntilChanged()
                .collect { sendTime ->
                    enqueue(sendTime, policy)
                    policy = ExistingWorkPolicy.REPLACE
                }
        }
    }

    /** 알림을 보낸 뒤 · 기기 시각이나 시간대가 바뀌었을 때 다음 알림 시각으로 다시 잡는다 */
    suspend fun scheduleNext() {
        enqueue(settingsRepository.observeNotificationSettings().first().sendTime, ExistingWorkPolicy.REPLACE)
    }

    private fun enqueue(
        sendTime: LocalTime,
        policy: ExistingWorkPolicy,
    ) {
        val request =
            OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayUntilNextReminder(ZonedDateTime.now(clock), sendTime))
                .build()
        WorkManager.getInstance(context).enqueueUniqueWork(REMINDER_WORK, policy, request)
    }

    private companion object {
        const val REMINDER_WORK = "daily_reminder"
    }
}

/** 다음 [sendTime](그 지역 시간 기준)까지 남은 시간. 그 시각이 지났으면(정각 포함) 다음 날. 서머타임으로 하루가 23 · 25시간이어도 맞게 센다 */
internal fun delayUntilNextReminder(
    now: ZonedDateTime,
    sendTime: LocalTime,
): Duration {
    val today = now.toLocalDate()
    val todayAt = today.atTime(sendTime).atZone(now.zone)
    val next = if (now.isBefore(todayAt)) todayAt else today.plusDays(1).atTime(sendTime).atZone(now.zone)
    return Duration.between(now, next)
}
