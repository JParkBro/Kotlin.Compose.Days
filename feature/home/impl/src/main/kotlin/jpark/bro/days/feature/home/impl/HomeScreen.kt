package jpark.bro.days.feature.home.impl

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysFab
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.ui.AddItemSheet
import jpark.bro.days.core.ui.DdayItemRow
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.home.impl.components.CoupleBanner
import jpark.bro.days.feature.home.impl.components.CoupleHeroCard
import jpark.bro.days.feature.home.impl.components.CoupleSummaryRow
import jpark.bro.days.feature.home.impl.components.HeroCard
import jpark.bro.days.feature.home.impl.components.HomeEmptyContent
import jpark.bro.days.feature.home.impl.components.HomeFilterChips
import jpark.bro.days.feature.home.impl.components.HomeListHeader
import jpark.bro.days.feature.home.impl.components.NextPlanCard
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
                        itemId = event.itemId,
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
    val emptyScrollState = rememberScrollState()
    val listState = rememberLazyListState()
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DaysTopBar(
                title = stringResource(R.string.home_main_title),
                isScrolled = if (state.isEmpty) emptyScrollState.canScrollBackward else listState.canScrollBackward,
                actions = {
                    DaysIconButton(
                        icon = DesignR.drawable.ic_notifications,
                        contentDescription = stringResource(R.string.home_main_notifications),
                        onClick = { onAction(HomeAction.NotificationsClicked) },
                        modifier = Modifier.testTag("home_notifications"),
                    )
                },
            )
            when {
                state.isLoading -> Unit
                state.isEmpty -> HomeEmpty(state = state, onAction = onAction, scrollState = emptyScrollState)
                else -> HomeList(state = state, onAction = onAction, listState = listState)
            }
        }
        if (!state.isLoading && !state.isEmpty) {
            DaysFab(
                icon = DesignR.drawable.ic_add,
                contentDescription = stringResource(R.string.home_add),
                onClick = { onAction(HomeAction.FabClicked) },
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(AppSpacing.lg)
                        .testTag("home_fab"),
            )
        }
    }
    if (state.isAddSheetOpen) {
        AddItemSheet(
            onSelect = { onAction(HomeAction.AddClicked(it)) },
            onDismiss = { onAction(HomeAction.AddSheetDismissed) },
        )
    }
}

@Composable
private fun ColumnScope.HomeEmpty(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    scrollState: ScrollState,
) {
    Column(
        modifier =
            Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = AppSpacing.marginMobile)
                .padding(top = AppSpacing.xl, bottom = AppSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
    ) {
        state.content.couple?.let { CoupleHeroCard(couple = it) }
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
}

/** 히어로 → 다가오는 일정 (커플이면 + 다음 기념일) → 커플 연결 카드 → (필터) → 디데이 리스트 (product-spec 4-1) */
@Composable
private fun ColumnScope.HomeList(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    listState: LazyListState,
) {
    val content = state.content
    val openItem: (Item) -> Unit = { onAction(HomeAction.ItemClicked(it.id, it.content.type)) }
    LazyColumn(
        state = listState,
        modifier = Modifier.weight(1f),
        // FAB에 마지막 항목이 가리지 않도록 아래 여백을 FAB 높이만큼 둔다
        contentPadding =
            PaddingValues(
                start = AppSpacing.marginMobile,
                end = AppSpacing.marginMobile,
                top = AppSpacing.sm,
                bottom = FabSpace,
            ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        content.hero?.let { hero ->
            item(key = "hero") { HeroCard(hero = hero, onClick = { openItem(hero.entry.item) }) }
        }
        val couple = content.couple
        if (couple != null) {
            item(key = "couple_hero") { CoupleHeroCard(couple = couple) }
            item(key = "couple_summary") {
                CoupleSummaryRow(
                    plan = content.nextPlan,
                    couple = couple,
                    onPlanClick = openItem,
                    onAddPlanClick = { onAction(HomeAction.AddClicked(ItemType.PLAN)) },
                )
            }
        } else {
            item(key = "next_plan") {
                NextPlanCard(
                    plan = content.nextPlan,
                    onPlanClick = openItem,
                    onAddClick = { onAction(HomeAction.AddClicked(ItemType.PLAN)) },
                )
            }
        }
        if (state.showCoupleBanner) {
            item(key = "couple_banner") {
                CoupleBanner(
                    onConnectClick = { onAction(HomeAction.ConnectCoupleClicked) },
                    onCloseClick = { onAction(HomeAction.CoupleBannerDismissed) },
                )
            }
        }
        homeRows(state = state, onAction = onAction, openItem = openItem)
    }
}

/** 리스트 제목 · (커플이면 필터) · 디데이 줄 */
private fun LazyListScope.homeRows(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    openItem: (Item) -> Unit,
) {
    if (state.content.rows.isEmpty()) return
    val rows = state.visibleRows
    item(key = "list_header") {
        HomeListHeader(count = rows.size, modifier = Modifier.padding(top = AppSpacing.sm))
    }
    if (state.isCoupled) {
        item(key = "filter") {
            HomeFilterChips(selected = state.filter, onSelect = { onAction(HomeAction.FilterSelected(it)) })
        }
        if (rows.isEmpty()) {
            item(key = "filter_empty") {
                Text(
                    text = stringResource(R.string.home_filter_empty),
                    style = AppThemeTokens.typography.bodyMd,
                    color = AppThemeTokens.colors.textSecondary,
                    modifier = Modifier.padding(AppSpacing.md),
                )
            }
        }
    }
    items(items = rows, key = { it.item.id }) { row ->
        DdayItemRow(
            content = row.item.content,
            dday = row.dday,
            onClick = { openItem(row.item) },
            showVisibility = state.isCoupled,
            modifier = Modifier.testTag("home_item_${row.item.id}"),
        )
    }
}

/** FAB(56dp) + 바깥 여백 */
private val FabSpace = 96.dp

@ScreenPreviews
@Composable
private fun HomeSoloPreview() {
    AppTheme { HomeScreen(state = previewState(pinnedItemId = "discharge"), onAction = {}) }
}

@ScreenPreviews
@Composable
private fun HomeSoloNoPinPreview() {
    AppTheme { HomeScreen(state = previewState(pinnedItemId = null).copy(showCoupleBanner = false), onAction = {}) }
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
private fun HomeCouplePreview() {
    AppTheme { HomeScreen(state = previewCoupleState(), onAction = {}) }
}
