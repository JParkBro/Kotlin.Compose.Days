package jpark.bro.days.feature.item.impl.itemdetail

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.HomeWidgets
import jpark.bro.days.core.data.repository.ImageRepository
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.data.repository.observeImageFileOrNull
import jpark.bro.days.core.domain.anniversary.itemMilestones
import jpark.bro.days.core.domain.dday.calculateDday
import jpark.bro.days.core.domain.dday.ddayOn
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.item.api.ItemDetailKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.Clock
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
internal class ItemDetailViewModel(
    key: ItemDetailKey,
    itemRepository: ItemRepository,
    imageRepository: ImageRepository,
    private val homeWidgets: HomeWidgets,
    private val clock: Clock,
) : MviViewModel<ItemDetailState, ItemDetailAction, ItemDetailEvent>(ItemDetailState(id = key.id)) {
    init {
        // 수정하고 돌아오면 바로 반영되고, 삭제되면 홈으로 돌아간다
        itemRepository
            .observeItem(key.id)
            .onEach { item ->
                if (item == null) {
                    sendEvent(ItemDetailEvent.NavigateBack)
                } else {
                    setState {
                        item.toDetailState(today = LocalDate.now(clock)).copy(
                            canAddWidget = homeWidgets.isPinSupported(),
                            isWidgetSheetOpen = isWidgetSheetOpen,
                            backgroundFile = backgroundFile,
                        )
                    }
                }
            }.launchIn(viewModelScope)
        itemRepository
            .observeItem(key.id)
            .map { it?.content?.backgroundImageId }
            .distinctUntilChanged()
            .flatMapLatest(imageRepository::observeImageFileOrNull)
            .onEach { file -> setState { copy(backgroundFile = file) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: ItemDetailAction) {
        when (action) {
            ItemDetailAction.EditClicked -> {
                currentState.item?.let { sendEvent(ItemDetailEvent.OpenEdit(it.id, it.content.type)) }
            }

            ItemDetailAction.BackClicked -> {
                sendEvent(ItemDetailEvent.NavigateBack)
            }

            ItemDetailAction.AddWidgetClicked -> {
                setState { copy(isWidgetSheetOpen = true) }
            }

            is ItemDetailAction.WidgetSizeSelected -> {
                setState { copy(isWidgetSheetOpen = false) }
                if (!homeWidgets.requestPin(
                        itemId = currentState.id,
                        size = action.size,
                    )
                ) {
                    sendEvent(ItemDetailEvent.WidgetAddFailed)
                }
            }

            ItemDetailAction.WidgetSheetDismissed -> {
                setState { copy(isWidgetSheetOpen = false) }
            }
        }
    }
}

/** 항목 → 화면 상태 (오늘 기준 D-day · 타임라인). Preview도 같은 변환을 쓴다 */
internal fun Item.toDetailState(today: LocalDate): ItemDetailState {
    val milestones = itemMilestones(content, today)
    val nextIndex = milestones.indexOfFirst { !it.date.isBefore(today) }
    return ItemDetailState(
        id = id,
        isLoading = false,
        item = this,
        dday = content.ddayOn(today),
        milestones =
            milestones.mapIndexed { index, anniversary ->
                DetailMilestone(
                    anniversary = anniversary,
                    dday = calculateDday(anniversary.date, CountMode.DDAY, false, RepeatRule.NONE, today),
                    isPassed = anniversary.date.isBefore(today),
                    isNext = index == nextIndex,
                )
            },
    )
}
