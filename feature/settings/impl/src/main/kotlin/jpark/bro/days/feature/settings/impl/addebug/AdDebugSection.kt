package jpark.bro.days.feature.settings.impl.addebug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.ads.rememberRewardedAdLauncher
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysSecondaryButton
import jpark.bro.days.core.designsystem.component.DaysSection
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.model.ImageUploadQuota
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.settings.impl.R
import org.koin.compose.viewmodel.koinViewModel

/** 디버그 빌드 전용 "광고 테스트" 섹션 (설정 맨 아래). 출시 빌드에는 그리지 않는다 */
@Composable
internal fun AdDebugRoot(viewModel: AdDebugViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val showRewardedAd = rememberRewardedAdLauncher { viewModel.onAction(AdDebugAction.RewardedAdFinished(it)) }

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is AdDebugEvent.ShowRewardedAd -> showRewardedAd(event.request)
        }
    }

    AdDebugSection(state = state, onAction = viewModel::onAction)
}

@Composable
private fun AdDebugSection(
    state: AdDebugState,
    onAction: (AdDebugAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    val watchLabel = if (state.isWatching) R.string.settings_debug_ad_watching else R.string.settings_debug_ad_watch
    DaysSection(title = stringResource(R.string.settings_debug_ad_section), modifier = modifier.testTag("ad_debug")) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = state.quotaText(),
                style = typography.bodyMd,
                color = colors.textPrimary,
                modifier = Modifier.testTag("ad_debug_quota"),
            )
            state.message?.let { message ->
                Text(text = stringResource(message.label), style = typography.bodySm, color = colors.textSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                DaysSecondaryButton(
                    text = stringResource(R.string.settings_debug_ad_refresh),
                    onClick = { onAction(AdDebugAction.RefreshClicked) },
                    enabled = !state.isLoading && !state.isWatching,
                    size = DaysButtonSize.SMALL,
                    modifier = Modifier.testTag("ad_debug_refresh"),
                )
                DaysSoftButton(
                    text = stringResource(watchLabel),
                    onClick = { onAction(AdDebugAction.WatchAdClicked) },
                    enabled = state.quota != null && !state.isLoading && !state.isWatching,
                    size = DaysButtonSize.SMALL,
                    modifier = Modifier.testTag("ad_debug_watch"),
                )
            }
        }
    }
}

@Composable
private fun AdDebugState.quotaText(): String {
    val quota = quota
    return when {
        isLoading -> stringResource(R.string.settings_debug_ad_loading)
        quota == null -> stringResource(R.string.settings_debug_ad_no_quota)
        else -> stringResource(R.string.settings_debug_ad_quota, quota.used, quota.limit, quota.bonus, quota.remaining)
    }
}

private val AdDebugMessage.label: Int
    get() =
        when (this) {
            AdDebugMessage.NOT_COUPLED -> R.string.settings_debug_ad_not_coupled
            AdDebugMessage.LOAD_FAILED -> R.string.settings_debug_ad_load_failed
            AdDebugMessage.AD_DISMISSED -> R.string.settings_debug_ad_dismissed
            AdDebugMessage.AD_FAILED -> R.string.settings_debug_ad_failed
            AdDebugMessage.REWARD_GRANTED -> R.string.settings_debug_ad_granted
            AdDebugMessage.REWARD_PENDING -> R.string.settings_debug_ad_pending
        }

@ComponentPreviews
@Composable
private fun AdDebugSectionPreview() {
    AppTheme {
        AdDebugSection(
            state =
                AdDebugState(
                    quota = ImageUploadQuota(userId = "me", coupleId = "c1", used = 30, bonus = 1),
                    message = AdDebugMessage.REWARD_GRANTED,
                ),
            onAction = {},
        )
    }
}
