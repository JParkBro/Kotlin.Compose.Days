package jpark.bro.days.feature.couple.impl.couplemanage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import jpark.bro.days.core.designsystem.component.DaysBottomSheet
import jpark.bro.days.core.designsystem.component.DaysCheckbox
import jpark.bro.days.core.designsystem.component.DaysDangerButton
import jpark.bro.days.core.designsystem.component.DaysSecondaryButton
import jpark.bro.days.feature.couple.impl.R
import jpark.bro.days.feature.couple.impl.couplemanage.CoupleManageAction
import jpark.bro.days.feature.couple.impl.couplemanage.CoupleManageState
import jpark.bro.days.core.designsystem.R as DesignR

/** 연결 해제 시트 (시안 couple-unpair-sheet). 상대에게 보내는 해제 알림은 알림 단계(7)에서 */
@Composable
internal fun UnpairSheet(
    partnerName: String,
    state: CoupleManageState,
    onAction: (CoupleManageAction) -> Unit,
) {
    val colors = AppThemeTokens.colors
    DaysBottomSheet(onDismissRequest = { onAction(CoupleManageAction.UnpairDismissed) }) {
        Column(
            modifier =
                Modifier
                    .padding(horizontal = AppSpacing.marginMobile)
                    .navigationBarsPadding()
                    .padding(bottom = AppSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(56.dp)
                        .clip(AppShapes.full)
                        .background(colors.dangerSoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(DesignR.drawable.ic_heart_broken), contentDescription = null, tint = colors.danger)
            }
            Text(
                text = stringResource(R.string.couple_unpair_title, partnerName),
                style = AppThemeTokens.typography.headlineSm,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.couple_unpair_subtitle),
                style = AppThemeTokens.typography.bodyMd,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            SheetBox(icon = DesignR.drawable.ic_history) {
                Text(
                    text = stringResource(R.string.couple_unpair_restore_title),
                    style = AppThemeTokens.typography.labelLg,
                    color = colors.textPrimary,
                )
                Text(
                    text = stringResource(R.string.couple_unpair_restore_description),
                    style = AppThemeTokens.typography.bodySm,
                    color = colors.textSecondary,
                )
            }
            SheetBox(
                leading = {
                    DaysCheckbox(
                        checked = state.deleteCopies,
                        onCheckedChange = { onAction(CoupleManageAction.DeleteCopiesChanged(it)) },
                        modifier = Modifier.testTag("unpair_delete_copies"),
                    )
                },
            ) {
                Text(
                    text = stringResource(R.string.couple_unpair_delete_copies),
                    style = AppThemeTokens.typography.labelLg,
                    color = colors.textPrimary,
                )
                Text(
                    text = stringResource(R.string.couple_unpair_delete_copies_description),
                    style = AppThemeTokens.typography.bodySm,
                    color = colors.textSecondary,
                )
            }
            DaysDangerButton(
                text = stringResource(R.string.couple_unpair_confirm),
                onClick = { onAction(CoupleManageAction.UnpairConfirmed) },
                enabled = !state.isWorking,
                filled = true,
                modifier = Modifier.fillMaxWidth().testTag("unpair_confirm"),
            )
            DaysSecondaryButton(
                text = stringResource(R.string.couple_unpair_cancel),
                onClick = { onAction(CoupleManageAction.UnpairDismissed) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SheetBox(
    icon: Int? = null,
    leading: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = AppThemeTokens.colors
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(AppShapes.default)
                .background(colors.primarySoft)
                .padding(AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        if (icon != null) Icon(painterResource(icon), contentDescription = null, tint = colors.onPrimarySoft)
        leading?.invoke()
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) { content() }
    }
}
