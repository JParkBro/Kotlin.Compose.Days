package jpark.bro.days.feature.item.impl.itemdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import jpark.bro.days.feature.item.impl.R
import jpark.bro.days.feature.item.impl.itemdetail.components.DetailHeroCard
import jpark.bro.days.feature.item.impl.itemdetail.components.MemoCard
import jpark.bro.days.feature.item.impl.itemdetail.components.MilestoneTimeline
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun ItemDetailRoot(
    key: ItemDetailKey,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    viewModel: ItemDetailViewModel = koinViewModel { parametersOf(key) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is ItemDetailEvent.OpenEdit -> {
                onNavigate(
                    ItemEditKey(itemId = event.id, type = event.type, presetDate = null),
                )
            }

            ItemDetailEvent.NavigateBack -> {
                onBack()
            }
        }
    }

    ItemDetailScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

/** 디데이 상세 (product-spec 4-4) */
@Composable
private fun ItemDetailScreen(
    state: ItemDetailState,
    onAction: (ItemDetailAction) -> Unit,
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
            title =
                state.item
                    ?.content
                    ?.title
                    .orEmpty(),
            isScrolled = scrollState.canScrollBackward,
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.item_detail_back),
                    onClick = { onAction(ItemDetailAction.BackClicked) },
                )
            },
            actions = {
                if (state.item != null) {
                    DaysSoftButton(
                        text = stringResource(R.string.item_detail_edit),
                        onClick = { onAction(ItemDetailAction.EditClicked) },
                        size = DaysButtonSize.SMALL,
                        modifier = Modifier.testTag("item_detail_edit"),
                    )
                }
            },
        )
        val item = state.item ?: return@Column
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = AppSpacing.marginMobile)
                    .padding(top = AppSpacing.sm, bottom = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            DetailHeroCard(item = item, dday = state.dday)
            item.content.memo?.let { memo ->
                MemoCard(memo = memo, onEditClick = { onAction(ItemDetailAction.EditClicked) })
            }
            if (state.milestones.isNotEmpty()) MilestoneTimeline(milestones = state.milestones)
        }
    }
}

@ScreenPreviews
@Composable
private fun ItemDetailQuitSmokingPreview() {
    AppTheme { ItemDetailScreen(state = PreviewQuitSmoking, onAction = {}) }
}

@ScreenPreviews
@Composable
private fun ItemDetailDischargePreview() {
    AppTheme { ItemDetailScreen(state = PreviewDischarge, onAction = {}) }
}

@ScreenPreviews
@Composable
private fun ItemDetailPlanPreview() {
    AppTheme { ItemDetailScreen(state = PreviewDinner, onAction = {}) }
}
