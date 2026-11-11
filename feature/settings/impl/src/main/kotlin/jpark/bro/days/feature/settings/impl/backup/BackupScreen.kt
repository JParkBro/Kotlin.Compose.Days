package jpark.bro.days.feature.settings.impl.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.ads.rememberRewardedAdLauncher
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysInfoBox
import jpark.bro.days.core.designsystem.component.DaysInfoBoxTone
import jpark.bro.days.core.designsystem.component.DaysPrimaryButton
import jpark.bro.days.core.designsystem.component.DaysSecondaryButton
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.core.ui.toDisplayText
import jpark.bro.days.feature.settings.impl.R
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDateTime
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun BackupRoot(
    onBack: () -> Unit,
    viewModel: BackupViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val showRewardedAd = rememberRewardedAdLauncher { viewModel.onAction(BackupAction.RewardedAdFinished(it)) }

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            BackupEvent.ShowRewardedAd -> {
                showRewardedAd(null)
            }

            BackupEvent.BackedUp -> {
                toastHostState.showSnackbar(resources.getString(R.string.settings_backup_done))
            }

            BackupEvent.BackupTooSoon -> {
                toastHostState.showSnackbar(resources.getString(R.string.settings_backup_too_soon))
            }

            BackupEvent.BackupFailed -> {
                toastHostState.showSnackbar(resources.getString(R.string.settings_backup_failed))
            }

            is BackupEvent.Restored -> {
                toastHostState.showSnackbar(resources.getString(R.string.settings_backup_restored, event.itemCount))
            }

            BackupEvent.NoBackup -> {
                toastHostState.showSnackbar(resources.getString(R.string.settings_backup_none))
            }

            BackupEvent.RestoreFailed -> {
                toastHostState.showSnackbar(resources.getString(R.string.settings_backup_restore_failed))
            }

            BackupEvent.NavigateBack -> {
                onBack()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BackupScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 백업 · 복원 (product-spec 6장): 나만 보기 항목과 대표 고정 · 알림을 서버에 백업하고 되살린다 */
@Composable
private fun BackupScreen(
    state: BackupState,
    onAction: (BackupAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.bg),
    ) {
        DaysTopBar(
            title = stringResource(R.string.settings_backup_title),
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.settings_backup_back),
                    onClick = { onAction(BackupAction.BackClicked) },
                )
            },
        )
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            Text(
                text = stringResource(R.string.settings_backup_desc),
                style = AppThemeTokens.typography.bodyMd,
                color = colors.textSecondary,
            )
            DaysInfoBox(text = stringResource(R.string.settings_backup_notice_photos))
            Text(
                text = state.lastBackupText(),
                style = AppThemeTokens.typography.labelMd,
                color = colors.textPrimary,
                modifier = Modifier.testTag("backup_last"),
            )
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                DaysPrimaryButton(
                    text =
                        stringResource(
                            if (state.work == BackupWork.BACKUP) {
                                R.string.settings_backup_working
                            } else {
                                R.string.settings_backup_button
                            },
                        ),
                    onClick = { onAction(BackupAction.BackupClicked) },
                    enabled = !state.isLoading && !state.isWorking,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag("backup_backup"),
                )
                Text(
                    text = stringResource(R.string.settings_backup_ad_notice),
                    style = AppThemeTokens.typography.bodySm,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = AppSpacing.xs),
                )
            }
            DaysSecondaryButton(
                text =
                    stringResource(
                        if (state.work == BackupWork.RESTORE) {
                            R.string.settings_backup_restore_working
                        } else {
                            R.string.settings_backup_restore_button
                        },
                    ),
                onClick = { onAction(BackupAction.RestoreClicked) },
                enabled = !state.isLoading && !state.isWorking,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("backup_restore"),
            )
        }
    }
    if (state.isRestoreConfirmVisible) {
        RestoreConfirmDialog(
            onConfirm = { onAction(BackupAction.RestoreConfirmed) },
            onDismiss = { onAction(BackupAction.RestoreDismissed) },
        )
    }
}

@Composable
private fun BackupState.lastBackupText(): String {
    val last = lastBackupAt
    return when {
        isLoading -> {
            stringResource(R.string.settings_backup_last_loading)
        }

        last == null -> {
            stringResource(R.string.settings_backup_last_none)
        }

        else -> {
            stringResource(
                R.string.settings_backup_last,
                last.toLocalDate().toDisplayText(),
                last.toLocalTime().toDisplayText(),
            )
        }
    }
}

@Composable
private fun RestoreConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppThemeTokens.colors.surfaceRaised,
        title = { Text(stringResource(R.string.settings_backup_restore_confirm_title)) },
        text = {
            DaysInfoBox(
                text = stringResource(R.string.settings_backup_restore_confirm_message),
                tone = DaysInfoBoxTone.WARNING,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.testTag("backup_restore_confirm")) {
                Text(stringResource(R.string.settings_backup_restore_button), color = AppThemeTokens.colors.danger)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(DesignR.string.common_cancel)) } },
    )
}

@ScreenPreviews
@Composable
private fun BackupScreenPreview() {
    AppTheme {
        BackupScreen(
            state = BackupState(isLoading = false, lastBackupAt = LocalDateTime.of(2026, 11, 5, 21, 30)),
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun BackupScreenRestoreConfirmPreview() {
    AppTheme {
        BackupScreen(
            state = BackupState(isLoading = false, isRestoreConfirmVisible = true),
            onAction = {},
        )
    }
}
