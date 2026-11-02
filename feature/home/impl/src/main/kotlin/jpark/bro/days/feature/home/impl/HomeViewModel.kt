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
    private val clock: Clock,
) : MviViewModel<HomeState, HomeAction, HomeEvent>(HomeState()) {
    init {
        combine(
            itemRepository.observeItems(),
            itemRepository.observePinnedItemId(),
            settingsRepository.observeCoupleBannerDismissed(),
        ) { items, pinnedItemId, bannerDismissed ->
            val today = LocalDate.now(clock)
            setState {
                copy(
                    isLoading = false,
                    hasItems = items.isNotEmpty(),
                    today = today,
                    content = buildHomeContent(items, pinnedItemId, today),
                    // 커플 연결은 아직 없으므로 항상 솔로: 닫지 않았으면 연결 카드를 보여준다
                    showCoupleBanner = !bannerDismissed,
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
