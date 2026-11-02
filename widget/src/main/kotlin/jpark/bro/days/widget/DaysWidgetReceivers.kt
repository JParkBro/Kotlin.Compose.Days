package jpark.bro.days.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import jpark.bro.days.core.data.repository.WidgetConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** 위젯 리시버 공통: 위젯을 지우면 설정도 지우고, 처음 놓이면 자정 갱신을 예약한다 */
abstract class DaysWidgetReceiver :
    GlanceAppWidgetReceiver(),
    KoinComponent {
    private val widgetConfigRepository: WidgetConfigRepository by inject()
    private val widgetUpdater: WidgetUpdater by inject()
    private val scope: CoroutineScope by inject()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        runAsync { widgetUpdater.scheduleNextMidnight() }
    }

    override fun onDeleted(
        context: Context,
        appWidgetIds: IntArray,
    ) {
        super.onDeleted(context, appWidgetIds)
        runAsync {
            widgetConfigRepository.deleteConfigs(appWidgetIds.toList())
            widgetUpdater.scheduleNextMidnight()
        }
    }

    private fun runAsync(block: suspend () -> Unit) {
        val pending = goAsync()
        scope.launch {
            try {
                block()
            } finally {
                pending.finish()
            }
        }
    }
}

class SmallDdayWidgetReceiver : DaysWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallDdayWidget()
}

class MediumDdayWidgetReceiver : DaysWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumDdayWidget()
}

/** 기기 시각 · 시간대가 바뀌면 D-day를 다시 그리고 자정 예약을 다시 잡는다 */
class WidgetTimeChangedReceiver :
    BroadcastReceiver(),
    KoinComponent {
    private val widgetUpdater: WidgetUpdater by inject()
    private val scope: CoroutineScope by inject()

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action !in TimeActions) return
        val pending = goAsync()
        scope.launch {
            try {
                widgetUpdater.updateAll()
                widgetUpdater.scheduleNextMidnight()
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        val TimeActions = setOf(Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)
    }
}
