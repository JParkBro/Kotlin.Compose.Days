package jpark.bro.days.feature.couple.impl.pairing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysInfoBox
import jpark.bro.days.core.designsystem.component.DaysInfoBoxTone
import jpark.bro.days.core.designsystem.component.DaysPrimaryButton
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.core.model.PairCode
import jpark.bro.days.feature.couple.impl.R
import jpark.bro.days.feature.couple.impl.components.PairCodeInput
import jpark.bro.days.core.designsystem.R as DesignR

// 커플 연결 화면의 두 탭 (내 코드 · 코드 입력)

@Composable
private fun Heading(
    title: String,
    subtitle: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Box(
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(AppShapes.full)
                    .background(AppThemeTokens.colors.primarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_favorite),
                contentDescription = null,
                tint = AppThemeTokens.colors.primary,
            )
        }
        Text(
            text = title,
            style = AppThemeTokens.typography.headlineSm,
            color = AppThemeTokens.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = subtitle,
            style = AppThemeTokens.typography.bodyMd,
            color = AppThemeTokens.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun MyCodeTab(
    state: PairingState,
    now: Long,
    onAction: (PairingAction) -> Unit,
) {
    Heading(
        title = stringResource(R.string.couple_pairing_my_code_title),
        subtitle = stringResource(R.string.couple_pairing_my_code_subtitle),
    )
    val code = state.myCode
    when {
        state.isIssuing -> {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppThemeTokens.colors.primary)
            }
        }

        state.issueError == IssueError.PROFILE_REQUIRED -> {
            DaysInfoBox(text = stringResource(R.string.couple_pairing_profile_required), tone = DaysInfoBoxTone.WARNING)
            DaysPrimaryButton(
                text = stringResource(R.string.couple_pairing_set_profile),
                onClick = { onAction(PairingAction.SetProfileClicked) },
                modifier = Modifier.fillMaxWidth().testTag("pairing_set_profile"),
            )
        }

        state.issueError == IssueError.FAILED || code == null -> {
            DaysInfoBox(text = stringResource(R.string.couple_pairing_issue_failed), tone = DaysInfoBoxTone.WARNING)
            DaysSoftButton(
                text = stringResource(R.string.couple_pairing_retry),
                onClick = { onAction(PairingAction.IssueClicked) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        else -> {
            CodeCard(code = code, now = now, onAction = onAction)
            Text(
                text = stringResource(R.string.couple_pairing_my_code_hint),
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CodeCard(
    code: PairCode,
    now: Long,
    onAction: (PairingAction) -> Unit,
) {
    val colors = AppThemeTokens.colors
    val remaining = code.expiresAt - now
    DaysCard {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.lg)
                        .background(colors.primarySoft)
                        .padding(vertical = AppSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = stringResource(R.string.couple_pairing_code_label),
                    style = AppThemeTokens.typography.labelMd,
                    color = colors.textSecondary,
                )
                Text(
                    text = code.code.chunked(HALF).joinToString(" "),
                    style = AppThemeTokens.typography.displayHeroMobile,
                    color = colors.onPrimarySoft,
                    modifier = Modifier.testTag("pairing_my_code"),
                )
                Text(
                    text =
                        if (remaining > 0) {
                            stringResource(R.string.couple_pairing_expires_in, remaining.toCountdown())
                        } else {
                            stringResource(R.string.couple_pairing_expired)
                        },
                    style = AppThemeTokens.typography.labelMd,
                    color = if (remaining > 0) colors.textSecondary else colors.danger,
                )
            }
            if (remaining > 0) {
                DaysSoftButton(
                    text = stringResource(R.string.couple_pairing_copy),
                    icon = DesignR.drawable.ic_content_copy,
                    onClick = { onAction(PairingAction.CopyClicked) },
                    modifier = Modifier.fillMaxWidth().testTag("pairing_copy"),
                )
            } else {
                DaysPrimaryButton(
                    text = stringResource(R.string.couple_pairing_new_code),
                    onClick = { onAction(PairingAction.IssueClicked) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
internal fun InputTab(
    state: PairingState,
    onAction: (PairingAction) -> Unit,
) {
    Heading(
        title = stringResource(R.string.couple_pairing_input_title),
        subtitle = stringResource(R.string.couple_pairing_input_subtitle),
    )
    PairCodeInput(
        cells = state.cells,
        activeCell = state.activeCell,
        onCellClick = { onAction(PairingAction.CellClicked(it)) },
        onType = { onAction(PairingAction.CharsTyped(it)) },
        onDelete = { onAction(PairingAction.DeletePressed) },
        onDone = { onAction(PairingAction.ConnectClicked) },
        modifier = Modifier.testTag("pairing_input"),
    )
    Text(
        text = state.inputError?.let { stringResource(it) } ?: stringResource(R.string.couple_pairing_input_rule),
        style = AppThemeTokens.typography.labelMd,
        color = if (state.inputError != null) AppThemeTokens.colors.danger else AppThemeTokens.colors.textSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().testTag("pairing_input_message"),
    )
    DaysInfoBox(text = stringResource(R.string.couple_pairing_input_hint), tone = DaysInfoBoxTone.TIP)
    DaysPrimaryButton(
        text = stringResource(R.string.couple_pairing_connect),
        onClick = { onAction(PairingAction.ConnectClicked) },
        enabled = state.canConnect,
        modifier = Modifier.fillMaxWidth().testTag("pairing_connect"),
    )
}

/** 23:41:05 */
private fun Long.toCountdown(): String {
    val totalSeconds = this / MILLIS_PER_SECOND
    val hours = totalSeconds / SECONDS_PER_HOUR
    val minutes = totalSeconds % SECONDS_PER_HOUR / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

private const val MILLIS_PER_SECOND = 1_000L
private const val SECONDS_PER_MINUTE = 60L
private const val SECONDS_PER_HOUR = 3_600L

/** 코드를 3자리씩 띄워 읽기 쉽게 */
private const val HALF = 3
