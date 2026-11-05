package jpark.bro.days.feature.couple.impl.paircodeconfirm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysDivider
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysInfoBox
import jpark.bro.days.core.designsystem.component.DaysInfoBoxTone
import jpark.bro.days.core.designsystem.component.DaysPrimaryButton
import jpark.bro.days.core.designsystem.component.DaysSecondaryButton
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.core.designsystem.component.DaysSwitch
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.ui.DaysDatePickerDialog
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.core.ui.toDisplayText
import jpark.bro.days.feature.couple.impl.PairCodeConfirmKey
import jpark.bro.days.feature.couple.impl.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun PairCodeConfirmRoot(
    key: PairCodeConfirmKey,
    onConnected: () -> Unit,
    onBack: () -> Unit,
    viewModel: PairCodeConfirmViewModel = koinViewModel { parametersOf(key) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val failed = stringResource(R.string.couple_confirm_failed)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            PairCodeConfirmEvent.Connected -> onConnected()
            PairCodeConfirmEvent.ConnectFailed -> toastHostState.showSnackbar(failed)
            PairCodeConfirmEvent.NavigateBack -> onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PairCodeConfirmScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 연결 확인 (시안 couple-confirm). 사진은 이미지 저장소 단계에서 */
@Composable
private fun PairCodeConfirmScreen(
    state: PairCodeConfirmState,
    onAction: (PairCodeConfirmAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.bg),
    ) {
        DaysTopBar(
            title = stringResource(R.string.couple_pairing_title),
            isScrolled = scrollState.canScrollBackward,
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.couple_back),
                    onClick = { onAction(PairCodeConfirmAction.BackClicked) },
                )
            },
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(64.dp)
                            .clip(AppShapes.full)
                            .background(colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(DesignR.drawable.ic_favorite),
                        contentDescription = null,
                        tint = colors.onPrimary,
                    )
                }
                Text(
                    text = stringResource(R.string.couple_confirm_title, state.partnerName),
                    style = AppThemeTokens.typography.headlineSm,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("confirm_title"),
                )
                Text(
                    text = stringResource(R.string.couple_confirm_subtitle),
                    style = AppThemeTokens.typography.bodyMd,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
            state.codeError?.let { DaysInfoBox(text = stringResource(it), tone = DaysInfoBoxTone.WARNING) }
            DaysCard {
                Row(
                    modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.couple_start_date),
                            style = AppThemeTokens.typography.labelMd,
                            color = colors.textSecondary,
                        )
                        Text(
                            text = state.startDate.toDisplayText(),
                            style = AppThemeTokens.typography.headlineSm,
                            color = colors.textPrimary,
                            modifier = Modifier.testTag("confirm_start_date"),
                        )
                    }
                    DaysSoftButton(
                        text = stringResource(R.string.couple_change),
                        onClick = { onAction(PairCodeConfirmAction.StartDateClicked) },
                        size = DaysButtonSize.SMALL,
                        modifier = Modifier.testTag("confirm_change_date"),
                    )
                }
                DaysDivider(inset = AppSpacing.marginMobile)
                Row(
                    modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.couple_count_first_day),
                            style = AppThemeTokens.typography.labelLg,
                            color = colors.textPrimary,
                        )
                        Text(
                            text = stringResource(R.string.couple_count_first_day_description),
                            style = AppThemeTokens.typography.bodySm,
                            color = colors.textSecondary,
                        )
                    }
                    DaysSwitch(
                        checked = state.countFirstDayAsOne,
                        onCheckedChange = { onAction(PairCodeConfirmAction.CountFirstDayChanged(it)) },
                        modifier = Modifier.testTag("confirm_count_first_day"),
                    )
                }
            }
            DaysInfoBox(
                text = stringResource(R.string.couple_together_days, state.togetherDays),
                tone = DaysInfoBoxTone.TIP,
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            DaysPrimaryButton(
                text = stringResource(R.string.couple_confirm_connect),
                onClick = { onAction(PairCodeConfirmAction.ConnectClicked) },
                enabled = state.canConnect,
                modifier = Modifier.fillMaxWidth().testTag("confirm_connect"),
            )
            DaysSecondaryButton(
                text = stringResource(R.string.couple_confirm_cancel),
                onClick = { onAction(PairCodeConfirmAction.BackClicked) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (state.isDatePickerOpen) {
        DaysDatePickerDialog(
            initial = state.startDate,
            maxDate = state.today,
            onConfirm = { onAction(PairCodeConfirmAction.StartDateSelected(it)) },
            onDismiss = { onAction(PairCodeConfirmAction.DatePickerDismissed) },
        )
    }
}

/** product-spec 9장: 사귄 날 2025.06.14, 오늘 2026.11.05 */
private val PreviewState =
    PairCodeConfirmState(
        code = "K7Q2MX",
        isLoading = false,
        partnerName = "지민",
        today = LocalDate.parse("2026-11-05"),
        startDate = LocalDate.parse("2025-06-14"),
    )

@ScreenPreviews
@Composable
private fun PairCodeConfirmPreview() {
    AppTheme { PairCodeConfirmScreen(state = PreviewState, onAction = {}) }
}

@ScreenPreviews
@Composable
private fun PairCodeConfirmExpiredPreview() {
    AppTheme {
        PairCodeConfirmScreen(
            state = PreviewState.copy(codeError = R.string.couple_code_expired),
            onAction = {},
        )
    }
}
