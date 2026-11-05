package jpark.bro.days.feature.onboarding.impl.startmode

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysBadge
import jpark.bro.days.core.designsystem.component.DaysBadgeTone
import jpark.bro.days.core.designsystem.component.DaysCheckbox
import jpark.bro.days.core.designsystem.component.DaysInfoBox
import jpark.bro.days.core.designsystem.component.DaysPrimaryButton
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.onboarding.api.OnboardingResult
import jpark.bro.days.feature.onboarding.impl.R
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.days.core.designsystem.R as DesignR

/** 시작 방식 선택 (product-spec 3-1): 혼자 → 홈, 커플 → 커플 연결 */
@Composable
internal fun StartModeRoot(
    onFinished: (OnboardingResult) -> Unit,
    viewModel: StartModeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is StartModeEvent.Finished -> onFinished(event.result)
        }
    }

    StartModeScreen(state = state, onAction = viewModel::onAction)
}

@Composable
private fun StartModeScreen(
    state: StartModeState,
    onAction: (StartModeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.bg)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = AppSpacing.marginMobile),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = AppSpacing.xxl),
        ) {
            Text(
                text = stringResource(R.string.onboarding_start_title),
                style = AppThemeTokens.typography.headlineLg,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.onboarding_start_subtitle),
                style = AppThemeTokens.typography.bodyMd,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = AppSpacing.sm, bottom = AppSpacing.xl),
            )
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                ModeCard(
                    icon = DesignR.drawable.ic_calendar,
                    title = stringResource(R.string.onboarding_start_solo),
                    description = stringResource(R.string.onboarding_start_solo_desc),
                    selected = state.selected == StartMode.SOLO,
                    onClick = { onAction(StartModeAction.ModeSelected(StartMode.SOLO)) },
                    modifier = Modifier.testTag("start_mode_solo"),
                )
                ModeCard(
                    icon = DesignR.drawable.ic_favorite,
                    title = stringResource(R.string.onboarding_start_couple),
                    description = stringResource(R.string.onboarding_start_couple_desc),
                    selected = state.selected == StartMode.COUPLE,
                    onClick = { onAction(StartModeAction.ModeSelected(StartMode.COUPLE)) },
                    recommended = true,
                    modifier = Modifier.testTag("start_mode_couple"),
                )
            }
            DaysInfoBox(
                text = stringResource(R.string.onboarding_start_later),
                modifier = Modifier.padding(top = AppSpacing.lg),
            )
        }
        DaysPrimaryButton(
            text = stringResource(R.string.onboarding_start_next),
            onClick = { onAction(StartModeAction.NextClicked) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacing.marginMobile)
                    .testTag("start_mode_next"),
        )
    }
}

@Composable
private fun ModeCard(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    recommended: Boolean = false,
) {
    val colors = AppThemeTokens.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShapes.lg)
                .background(if (selected) colors.primarySoft else colors.surface)
                .border(1.dp, if (selected) colors.primary else colors.border, AppShapes.lg)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .padding(AppSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(AppShapes.full)
                    .background(if (selected) colors.primary else colors.fill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (selected) colors.onPrimary else colors.textSecondary,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(text = title, style = AppThemeTokens.typography.headlineSm, color = colors.textPrimary)
                if (recommended) {
                    DaysBadge(text = stringResource(R.string.onboarding_start_recommended), tone = DaysBadgeTone.SHARED)
                }
            }
            Text(
                text = description,
                style = AppThemeTokens.typography.bodySm,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = AppSpacing.xs),
            )
        }
        DaysCheckbox(checked = selected, onCheckedChange = null)
    }
}

@ScreenPreviews
@Composable
private fun StartModeScreenPreview() {
    AppTheme { StartModeScreen(state = StartModeState(), onAction = {}) }
}

@ScreenPreviews
@Composable
private fun StartModeSoloPreview() {
    AppTheme { StartModeScreen(state = StartModeState(selected = StartMode.SOLO), onAction = {}) }
}
