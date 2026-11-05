package jpark.bro.days.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.data.repository.WidgetConfigRepository
import jpark.bro.days.core.model.WidgetBackground
import jpark.bro.days.core.model.WidgetSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Clock
import java.time.LocalDate

/** 위젯 한 번 그리기에 필요한 것 */
internal data class WidgetModel(
    val display: WidgetDisplay,
    val colors: WidgetColors,
    val photo: Bitmap?,
    val avatars: List<WidgetAvatar>,
)

/**
 * 디데이 위젯 (Glance). 크기마다 하위 클래스를 두어 위젯 고르기 화면에 두 크기가 따로 보이고,
 * updateAll이 크기별로 동작한다. 위젯은 Android 진입점이라 Koin에서 직접 꺼낸다 (CLAUDE.md 예외).
 * Glance는 세션이 열려 있는 동안 provideGlance를 다시 부르지 않으므로, 내용은 흐름으로 구독해 바뀌면 다시 그린다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class DaysWidget(
    private val size: WidgetSize,
) : GlanceAppWidget(),
    KoinComponent {
    private val itemRepository: ItemRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val widgetConfigRepository: WidgetConfigRepository by inject()
    private val photos: WidgetPhotos by inject()
    private val clock: Clock by inject()

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val models = observeModel(GlanceAppWidgetManager(context).getAppWidgetId(id))
        val initial = models.first()
        provideContent {
            val model by models.collectAsState(initial)
            DaysWidgetContent(
                display = model.display,
                size = size,
                colors = model.colors,
                photo = model.photo,
                avatars = model.avatars,
            )
        }
    }

    private fun observeModel(widgetId: Int): Flow<WidgetModel> =
        combine(
            itemRepository.observeItems(),
            itemRepository.observePinnedItemId(),
            settingsRepository.observeThemeSetting(),
            widgetConfigRepository.observeConfigs().map { configs -> configs.firstOrNull { it.widgetId == widgetId } },
        ) { items, pinnedItemId, theme, config ->
            Triple(buildWidgetDisplay(items, pinnedItemId, config, LocalDate.now(clock)), theme.type, config)
        }.mapLatest { (display, theme, config) ->
            val background = config?.background ?: WidgetBackground.THEME
            val photo =
                if (background == WidgetBackground.PHOTO) {
                    photos.background((display as? WidgetDisplay.Dday)?.entry?.item)?.decodeForWidget(PHOTO_MAX_EDGE)
                } else {
                    null
                }
            val avatars =
                if (config?.showProfile == true) {
                    photos.profiles().map { WidgetAvatar(it.name?.take(1), it.photo?.decodeForWidget(AVATAR_MAX_EDGE)) }
                } else {
                    emptyList()
                }
            WidgetModel(
                display = display,
                colors = widgetColorScheme(theme, background.effective(hasPhoto = photo != null)).toGlance(),
                photo = photo,
                avatars = avatars,
            )
        }.flowOn(Dispatchers.IO)

    private companion object {
        /** 위젯 비트맵은 RemoteViews 크기 한도가 있어 작게 읽는다 */
        const val PHOTO_MAX_EDGE = 720
        const val AVATAR_MAX_EDGE = 96
    }
}

/** 작게 (2×2) */
class SmallDdayWidget : DaysWidget(WidgetSize.SMALL)

/** 중간 (4×2) */
class MediumDdayWidget : DaysWidget(WidgetSize.MEDIUM)
