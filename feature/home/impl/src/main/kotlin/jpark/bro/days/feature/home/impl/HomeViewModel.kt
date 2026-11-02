package jpark.bro.days.feature.home.impl

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class HomeViewModel(
    itemRepository: ItemRepository,
    private val settingsRepository: SettingsRepository,
) : MviViewModel<HomeState, HomeAction, HomeEvent>(HomeState()) {
    init {
        combine(
            itemRepository.observeItems(),
            settingsRepository.observeCoupleBannerDismissed(),
        ) { items, bannerDismissed ->
            // 커플 연결은 아직 없으므로 항상 솔로: 닫지 않았으면 연결 카드를 보여준다
            HomeState(isLoading = false, items = items, showCoupleBanner = !bannerDismissed)
        }.onEach { state -> setState { state } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.ItemClicked -> {
                sendEvent(HomeEvent.OpenItemDetail(action.id))
            }

            is HomeAction.AddClicked -> {
                sendEvent(HomeEvent.OpenItemEdit(action.type))
            }

            is HomeAction.QuickStartClicked -> {
                sendEvent(HomeEvent.OpenItemEdit(ItemType.DDAY, action.quickStart))
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
