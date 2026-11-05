package jpark.bro.days.widget.config

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.data.repository.WidgetConfigRepository
import jpark.bro.days.core.domain.dday.countingItems
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.WidgetConfig
import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.widget.WidgetDisplay
import jpark.bro.days.widget.WidgetPhotos
import jpark.bro.days.widget.buildWidgetDisplay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

internal class WidgetConfigViewModel(
    target: WidgetTarget,
    private val itemRepository: ItemRepository,
    private val widgetConfigRepository: WidgetConfigRepository,
    private val settingsRepository: SettingsRepository,
    private val photos: WidgetPhotos,
    private val clock: Clock,
) : MviViewModel<WidgetConfigState, WidgetConfigAction, WidgetConfigEvent>(
        WidgetConfigState(target.widgetId, target.size),
    ) {
    private var items: List<Item> = emptyList()
    private var pinnedItemId: String? = null

    init {
        viewModelScope.launch {
            items = itemRepository.observeItems().first()
            pinnedItemId = itemRepository.observePinnedItemId().first()
            val saved = widgetConfigRepository.getConfig(target.widgetId)
            val today = LocalDate.now(clock)
            val theme = settingsRepository.observeThemeSetting().first().type
            val profiles = photos.profiles()
            update {
                copy(
                    isLoading = false,
                    choices = countingItems(items, today),
                    // 삭제된 디데이를 고른 상태였으면 자동으로 되돌린다
                    selectedItemId = saved?.itemId?.takeIf { id -> items.any { it.id == id } },
                    background = saved?.background ?: background,
                    showProfile = saved?.showProfile ?: showProfile,
                    theme = theme,
                    profiles = profiles,
                )
            }
        }
    }

    override fun onAction(action: WidgetConfigAction) {
        when (action) {
            is WidgetConfigAction.ItemSelected -> update { copy(selectedItemId = action.itemId) }
            is WidgetConfigAction.BackgroundSelected -> update { copy(background = action.background) }
            is WidgetConfigAction.ShowProfileChanged -> update { copy(showProfile = action.show) }
            WidgetConfigAction.SaveClicked -> save()
            WidgetConfigAction.CloseClicked -> sendEvent(WidgetConfigEvent.Close)
        }
    }

    /** 고른 값이 바뀌면 미리보기(디데이 · 배경 사진)도 다시 만든다 */
    private fun update(reduce: WidgetConfigState.() -> WidgetConfigState) {
        setState {
            val next = reduce()
            next.copy(display = buildWidgetDisplay(items, pinnedItemId, next.toConfig(), LocalDate.now(clock)))
        }
        val shown = (currentState.display as? WidgetDisplay.Dday)?.entry?.item
        viewModelScope.launch {
            val photo = photos.background(shown)
            setState { copy(photo = photo) }
        }
    }

    private fun save() {
        val state = currentState
        if (state.isLoading || state.isSaving) return
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            // 저장하면 WidgetUpdater가 위젯을 다시 그린다
            widgetConfigRepository
                .saveConfig(state.toConfig())
                .onSuccess { sendEvent(WidgetConfigEvent.Close) }
                .onFailure {
                    setState { copy(isSaving = false) }
                    sendEvent(WidgetConfigEvent.SaveFailed)
                }
        }
    }
}

private fun WidgetConfigState.toConfig() =
    WidgetConfig(
        widgetId = widgetId,
        size = size,
        itemId = selectedItemId,
        background = background,
        showProfile = showProfile,
    )
