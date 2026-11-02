package jpark.bro.days.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.data.repository.WidgetConfigRepository
import jpark.bro.days.core.model.WidgetBackground
import jpark.bro.days.core.model.WidgetSize
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Clock
import java.time.LocalDate

/**
 * 디데이 위젯 (Glance). 크기마다 하위 클래스를 두어 위젯 고르기 화면에 두 크기가 따로 보이고,
 * updateAll이 크기별로 동작한다. 위젯은 Android 진입점이라 Koin에서 직접 꺼낸다 (CLAUDE.md 예외).
 */
abstract class DaysWidget(
    private val size: WidgetSize,
) : GlanceAppWidget(),
    KoinComponent {
    private val itemRepository: ItemRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val widgetConfigRepository: WidgetConfigRepository by inject()
    private val clock: Clock by inject()

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val config = widgetConfigRepository.getConfig(widgetId)
        val display =
            buildWidgetDisplay(
                items = itemRepository.observeItems().first(),
                pinnedItemId = itemRepository.observePinnedItemId().first(),
                config = config,
                today = LocalDate.now(clock),
            )
        val colors =
            widgetColorScheme(
                theme = settingsRepository.observeThemeSetting().first().type,
                background = config?.background ?: WidgetBackground.THEME,
            ).toGlance()
        provideContent { DaysWidgetContent(display = display, size = size, colors = colors) }
    }
}

/** 작게 (2×2) */
class SmallDdayWidget : DaysWidget(WidgetSize.SMALL)

/** 중간 (4×2) */
class MediumDdayWidget : DaysWidget(WidgetSize.MEDIUM)
