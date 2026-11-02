package jpark.bro.days.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import jpark.bro.days.core.data.repository.HomeWidgets
import jpark.bro.days.core.data.repository.WidgetConfigRepository
import jpark.bro.days.core.model.WidgetConfig
import jpark.bro.days.core.model.WidgetSize
import jpark.bro.days.widget.config.WidgetConfigActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [HomeWidgets] 구현. 놓인 위젯 id는 시스템(AppWidgetManager)에, 설정은 widget_config에 있다 */
internal class DefaultHomeWidgets(
    private val context: Context,
    private val widgetConfigRepository: WidgetConfigRepository,
) : HomeWidgets {
    private val appWidgetManager: AppWidgetManager get() = AppWidgetManager.getInstance(context)

    override fun observePlacedWidgets(): Flow<List<WidgetConfig>> =
        widgetConfigRepository.observeConfigs().map { saved ->
            val byId = saved.associateBy { it.widgetId }
            WidgetSize.entries
                .flatMap { size -> appWidgetManager.getAppWidgetIds(context.providerOf(size)).map { it to size } }
                .sortedBy { it.first }
                .map { (id, size) -> byId[id] ?: WidgetConfig(widgetId = id, size = size) }
        }

    override fun isPinSupported(): Boolean = appWidgetManager.isRequestPinAppWidgetSupported

    override fun requestPin(
        itemId: String?,
        size: WidgetSize,
    ): Boolean {
        if (!isPinSupported()) return false
        // 시스템이 놓인 위젯 id를 넣어 보내야 하므로 MUTABLE. 대상은 앱 안의 리시버로 고정한다
        val callback =
            PendingIntent.getBroadcast(
                context,
                PIN_REQUEST_CODE,
                Intent(context, WidgetPinnedReceiver::class.java)
                    .putExtra(WidgetPinnedReceiver.EXTRA_ITEM_ID, itemId)
                    .putExtra(WidgetPinnedReceiver.EXTRA_SIZE, size.name),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
            )
        return appWidgetManager.requestPinAppWidget(context.providerOf(size), null, callback)
    }

    override fun openConfigure(widgetId: Int) {
        context.startActivity(
            Intent(context, WidgetConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    private companion object {
        const val PIN_REQUEST_CODE = 1
    }
}

/** 크기별 위젯 리시버 */
internal fun Context.providerOf(size: WidgetSize): ComponentName =
    when (size) {
        WidgetSize.SMALL -> ComponentName(this, SmallDdayWidgetReceiver::class.java)
        WidgetSize.MEDIUM -> ComponentName(this, MediumDdayWidgetReceiver::class.java)
    }
