package jpark.bro.days.feature.home.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.home.impl.components.CoupleBanner
import jpark.bro.days.feature.home.impl.components.HomeEmptyContent
import jpark.bro.days.feature.home.impl.components.label
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import jpark.bro.days.feature.notifications.api.NotificationsKey
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun HomeRoot(
    onNavigate: (NavKey) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // 빠른 시작 제목은 화면 문자열이라 Root에서 미리 꺼내 둔다
    val quickStartTitles = QuickStart.entries.associateWith { stringResource(it.label) }

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is HomeEvent.OpenItemDetail -> {
                onNavigate(ItemDetailKey(event.id))
            }

            is HomeEvent.OpenItemEdit -> {
                onNavigate(
                    ItemEditKey(
                        itemId = null,
                        type = event.type,
                        presetDate = null,
                        presetCategory = event.quickStart?.category,
                        presetTitle = event.quickStart?.let(quickStartTitles::getValue),
                    ),
                )
            }

            HomeEvent.OpenNotifications -> {
                onNavigate(NotificationsKey)
            }

            HomeEvent.OpenPairing -> {
                onNavigate(PairingKey)
            }
        }
    }

    HomeScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg),
    ) {
        DaysTopBar(
            title = stringResource(R.string.home_main_title),
            isScrolled = scrollState.canScrollBackward,
            actions = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_notifications,
                    contentDescription = stringResource(R.string.home_main_notifications),
                    onClick = { onAction(HomeAction.NotificationsClicked) },
                    modifier = Modifier.testTag("home_notifications"),
                )
            },
        )
        // 항목이 있는 홈(히어로 · 다음 일정 · 리스트)은 다음 작업에서 만든다
        if (state.isEmpty) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = AppSpacing.marginMobile)
                        .padding(top = AppSpacing.xl, bottom = AppSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
            ) {
                HomeEmptyContent(
                    onQuickStartClick = { onAction(HomeAction.QuickStartClicked(it)) },
                    onAddClick = { onAction(HomeAction.AddClicked(ItemType.DDAY)) },
                )
                if (state.showCoupleBanner) {
                    CoupleBanner(
                        onConnectClick = { onAction(HomeAction.ConnectCoupleClicked) },
                        onCloseClick = { onAction(HomeAction.CoupleBannerDismissed) },
                    )
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f))
        }
    }
}

@ScreenPreviews
@Composable
private fun HomeEmptyPreview() {
    AppTheme {
        HomeScreen(
            state = HomeState(isLoading = false, showCoupleBanner = true),
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun HomeEmptyBannerClosedPreview() {
    AppTheme {
        HomeScreen(
            state = HomeState(isLoading = false, showCoupleBanner = false),
            onAction = {},
        )
    }
}
