package jpark.bro.days.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import jpark.bro.days.core.data.repository.WidgetConfigRepository
import jpark.bro.days.core.model.WidgetConfig
import jpark.bro.days.core.model.WidgetSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** 위젯 리시버 공통: 놓이면 기본 설정을 저장하고, 지우면 설정도 지우고, 자정 갱신을 예약한다 */
abstract class DaysWidgetReceiver :
    GlanceAppWidgetReceiver(),
    KoinComponent {
    protected abstract val size: WidgetSize

    private val widgetConfigRepository: WidgetConfigRepository by inject()
    private val widgetUpdater: WidgetUpdater by inject()
    private val scope: CoroutineScope by inject()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        runAsync { widgetUpdater.scheduleNextMidnight() }
    }

    /** 위젯이 놓이면 기본 설정(홈 히어로 규칙 · 테마색)을 저장해 위젯 설정 목록에 보이게 한다. 이미 있으면 그대로 */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        runAsync {
            appWidgetIds.forEach { widgetConfigRepository.saveConfigIfAbsent(WidgetConfig(widgetId = it, size = size)) }
        }
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
    override val size: WidgetSize = WidgetSize.SMALL
    override val glanceAppWidget: GlanceAppWidget = SmallDdayWidget()
}

class MediumDdayWidgetReceiver : DaysWidgetReceiver() {
    override val size: WidgetSize = WidgetSize.MEDIUM
    override val glanceAppWidget: GlanceAppWidget = MediumDdayWidget()
}

/**
 * 앱에서 요청한 위젯 추가(requestPinAppWidget)가 홈 화면에 실제로 놓였을 때 시스템이 보내는 콜백.
 * 고른 디데이로 그 위젯을 설정한다. 콜백은 앱이 만든 PendingIntent로만 오므로 내보내지 않는다
 */
class WidgetPinnedReceiver :
    BroadcastReceiver(),
    KoinComponent {
    private val widgetConfigRepository: WidgetConfigRepository by inject()
    private val scope: CoroutineScope by inject()

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val size = intent.getStringExtra(EXTRA_SIZE)?.let { name -> WidgetSize.entries.firstOrNull { it.name == name } }
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID || size == null) return
        val pending = goAsync()
        scope.launch {
            try {
                // 위젯 설정이 바뀌면 WidgetUpdater가 다시 그린다
                widgetConfigRepository.saveConfig(
                    WidgetConfig(widgetId = widgetId, size = size, itemId = intent.getStringExtra(EXTRA_ITEM_ID)),
                )
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_ITEM_ID = "jpark.bro.days.widget.extra.ITEM_ID"
        const val EXTRA_SIZE = "jpark.bro.days.widget.extra.SIZE"
    }
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
