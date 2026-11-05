package jpark.bro.days.feature.home.impl

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.AnniversaryRepository
import jpark.bro.days.core.data.repository.CoupleRepository
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.model.Couple
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.UserProfile
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

internal class HomeViewModel(
    itemRepository: ItemRepository,
    private val settingsRepository: SettingsRepository,
    coupleRepository: CoupleRepository,
    profileRepository: ProfileRepository,
    anniversaryRepository: AnniversaryRepository,
    private val clock: Clock,
) : MviViewModel<HomeState, HomeAction, HomeEvent>(HomeState()) {
    init {
        val coupleSource =
            combine(
                coupleRepository.observeCouple(),
                profileRepository.observeMyProfile(),
                profileRepository.observePartnerProfile(),
                anniversaryRepository.observeHiddenRuleKeys(),
                ::CoupleSource,
            )
        combine(
            itemRepository.observeItems(),
            itemRepository.observePinnedItemId(),
            settingsRepository.observeCoupleBannerDismissed(),
            coupleSource,
        ) { items, pinnedItemId, bannerDismissed, source ->
            val today = LocalDate.now(clock)
            val couple =
                source.couple?.let { buildCoupleSummary(it, source.me, source.partner, source.hidden, today) }
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

private data class CoupleSource(
    val couple: Couple?,
    val me: UserProfile?,
    val partner: UserProfile?,
    val hidden: Set<String>,
)
