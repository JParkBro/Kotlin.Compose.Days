package jpark.bro.days.feature.home.impl

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

internal class HomeViewModel(
    itemRepository: ItemRepository,
    private val settingsRepository: SettingsRepository,
    coupleSource: HomeCoupleSource,
    private val clock: Clock,
) : MviViewModel<HomeState, HomeAction, HomeEvent>(HomeState()) {
    init {
        combine(
            itemRepository.observeItems(),
            itemRepository.observePinnedItemId(),
            settingsRepository.observeCoupleBannerDismissed(),
            coupleSource.observe(),
        ) { items, pinnedItemId, bannerDismissed, source ->
            val today = LocalDate.now(clock)
            val couple = buildCoupleSummary(source, today)
            setState {
                copy(
                    isLoading = false,
                    hasItems = items.isNotEmpty(),
                    today = today,
                    content = buildHomeContent(items, pinnedItemId, today, couple),
                    // 연결 유도 카드는 솔로이고 닫지 않았을 때만 (product-spec 4-1)
                    showCoupleBanner = couple == null && !bannerDismissed,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.ItemClicked -> {
                sendEvent(
                    when (action.type) {
                        ItemType.DDAY -> HomeEvent.OpenItemDetail(action.id)
                        ItemType.PLAN -> HomeEvent.OpenItemEdit(ItemType.PLAN, itemId = action.id)
                    },
                )
            }

            is HomeAction.AddClicked -> {
                setState { copy(isAddSheetOpen = false) }
                sendEvent(HomeEvent.OpenItemEdit(action.type))
            }

            is HomeAction.QuickStartClicked -> {
                sendEvent(HomeEvent.OpenItemEdit(ItemType.DDAY, quickStart = action.quickStart))
            }

            is HomeAction.FilterSelected -> {
                setState { copy(filter = action.filter) }
            }

            HomeAction.FabClicked -> {
                setState { copy(isAddSheetOpen = true) }
            }

            HomeAction.AddSheetDismissed -> {
                setState { copy(isAddSheetOpen = false) }
            }

            HomeAction.NotificationsClicked -> {
                sendEvent(HomeEvent.OpenNotifications)
            }

            HomeAction.ConnectCoupleClicked -> {
                sendEvent(HomeEvent.OpenPairing)
            }

            HomeAction.CoupleBannerDismissed -> {
                // 닫기 저장에 실패해도 화면에는 다음 실행 때 다시 보일 뿐이라 알리지 않는다
                viewModelScope.launch { settingsRepository.dismissCoupleBanner() }
            }
        }
    }
}
