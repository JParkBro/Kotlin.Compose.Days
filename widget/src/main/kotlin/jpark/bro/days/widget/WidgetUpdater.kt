package jpark.bro.days.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.data.repository.WidgetConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime

/**
 * 위젯 갱신 (module-map 10장).
 * - 항목 · 대표 고정 · 테마 · 위젯 설정이 바뀌면 전체 갱신 (앱 프로세스가 살아 있는 동안)
 * - 자정마다 갱신: WorkManager 1회 작업을 다음 자정으로 예약하고, 실행되면 다시 예약
 */
class WidgetUpdater(
    private val context: Context,
    private val itemRepository: ItemRepository,
    private val settingsRepository: SettingsRepository,
    private val widgetConfigRepository: WidgetConfigRepository,
    private val clock: Clock,
    private val scope: CoroutineScope,
) {
    /** 앱 시작 시 한 번 (Application) */
    fun start() {
        combine(
            itemRepository.observeItems(),
            itemRepository.observePinnedItemId(),
            settingsRepository.observeThemeSetting(),
            widgetConfigRepository.observeConfigs(),
        ) { _, _, _, _ -> }
            .onEach {
                updateAll()
                scheduleNextMidnight()
            }.launchIn(scope)
    }

    suspend fun updateAll() {
        SmallDdayWidget().updateAll(context)
        MediumDdayWidget().updateAll(context)
    }

    /** 놓인 위젯이 있을 때만 다음 자정 갱신을 예약한다 */
    suspend fun scheduleNextMidnight() {
        val manager = GlanceAppWidgetManager(context)
        val hasWidgets =
            manager.getGlanceIds(SmallDdayWidget::class.java).isNotEmpty() ||
                manager.getGlanceIds(MediumDdayWidget::class.java).isNotEmpty()
        val workManager = WorkManager.getInstance(context)
        if (!hasWidgets) {
            workManager.cancelUniqueWork(MIDNIGHT_WORK)
            return
        }
        val request =
            OneTimeWorkRequestBuilder<WidgetMidnightWorker>()
                .setInitialDelay(delayUntilNextMidnight(ZonedDateTime.now(clock)))
                .build()
        workManager.enqueueUniqueWork(MIDNIGHT_WORK, ExistingWorkPolicy.REPLACE, request)
    }

    private companion object {
        const val MIDNIGHT_WORK = "widget_midnight_update"
    }
}

/** 자정 직전에 깨어 어제 날짜로 그리지 않도록 조금 늦게 */
private val MidnightMargin: Duration = Duration.ofMinutes(1)

/** 다음 자정(그 지역 시간 기준) + 1분까지 남은 시간. 서머타임으로 하루가 23 · 25시간이어도 맞게 센다 */
internal fun delayUntilNextMidnight(now: ZonedDateTime): Duration {
    val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
    return Duration.between(now, nextMidnight).plus(MidnightMargin)
}
