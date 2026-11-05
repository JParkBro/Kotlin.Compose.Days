package jpark.bro.days.feature.couple.impl.anniversaries

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysInfoBox
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.component.DaysSwitch
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.component.ddayLabel
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.core.ui.title
import jpark.bro.days.core.ui.toDisplayText
import jpark.bro.days.feature.couple.impl.R
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun AnniversaryListRoot(
    onBack: () -> Unit,
    viewModel: AnniversaryListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val saveFailed = stringResource(R.string.couple_anniversaries_save_failed)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            AnniversaryListEvent.SaveFailed -> toastHostState.showSnackbar(saveFailed)
            AnniversaryListEvent.NavigateBack -> onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnniversaryListScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 앞으로 1년의 자동 기념일. 끄면 홈 · 알림에서 숨기고 상대 기기에도 숨겨진다 (product-spec 2-3) */
@Composable
private fun AnniversaryListScreen(
    state: AnniversaryListState,
    onAction: (AnniversaryListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg),
    ) {
        DaysTopBar(
            title = stringResource(R.string.couple_anniversaries_title),
            isScrolled = listState.canScrollBackward,
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.couple_back),
                    onClick = { onAction(AnniversaryListAction.BackClicked) },
                )
            },
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            item(key = "info") { DaysInfoBox(text = stringResource(R.string.couple_anniversaries_info)) }
            items(items = state.rows, key = { it.anniversary.ruleKey }) { row ->
                AnniversaryRowItem(row = row, nicknames = state.nicknames, onAction = onAction)
            }
        }
    }
}

@Composable
private fun AnniversaryRowItem(
    row: AnniversaryRow,
    nicknames: Map<String, String>,
    onAction: (AnniversaryListAction) -> Unit,
) {
    val ruleKey = row.anniversary.ruleKey
    DaysCard {
        DaysListItem(
            title = row.anniversary.title(nicknames::get),
            subtitle = "${row.anniversary.date.toDisplayText()} · ${ddayLabel(row.dday)}",
            // 숨긴 기념일은 흐리게 (지난 것 · 숨긴 것은 text-tertiary 톤)
            modifier = Modifier.alpha(if (row.isHidden) HIDDEN_ALPHA else 1f),
            trailing = {
                DaysSwitch(
                    checked = !row.isHidden,
                    onCheckedChange = { onAction(AnniversaryListAction.VisibilityChanged(ruleKey, it)) },
                    modifier = Modifier.testTag("anniversary_switch_$ruleKey"),
                )
            },
        )
    }
}

private const val HIDDEN_ALPHA = 0.5f

/** product-spec 9장: 사귄 날 2025.06.14 (1일 세기), 오늘 2026.11.05 */
@Suppress("MagicNumber") // 샘플 데이터
private val PreviewState =
    AnniversaryListState(
        isLoading = false,
        rows =
            listOf(
                AnniversaryRow(AutoAnniversary.Days(600, LocalDate.of(2027, 2, 3)), DdayCount.Remaining(90), false),
                AnniversaryRow(
                    AutoAnniversary.Birthday("user-me", 2027, LocalDate.of(2027, 4, 12)),
                    DdayCount.Remaining(158),
                    false,
                ),
                AnniversaryRow(AutoAnniversary.Days(700, LocalDate.of(2027, 5, 14)), DdayCount.Remaining(190), true),
                AnniversaryRow(AutoAnniversary.Years(2, LocalDate.of(2027, 6, 14)), DdayCount.Remaining(221), false),
            ),
        nicknames = mapOf("user-me" to "현우"),
    )

@ScreenPreviews
@Composable
private fun AnniversaryListPreview() {
    AppTheme { AnniversaryListScreen(state = PreviewState, onAction = {}) }
}
