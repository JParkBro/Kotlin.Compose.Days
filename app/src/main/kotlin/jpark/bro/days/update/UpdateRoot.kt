package jpark.bro.days.update

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.play.core.appupdate.AppUpdateInfo
import jpark.bro.days.R
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.ui.ObserveEvents

/**
 * 인앱 업데이트 다이얼로그. 다이얼로그는 별도 창이라 어느 화면 위에든 뜬다.
 * Play 호출은 Activity가 [onStartUpdateFlow] · [onCompleteUpdate]로 한다.
 */
@Composable
internal fun UpdateRoot(
    viewModel: UpdateViewModel,
    onStartUpdateFlow: (AppUpdateInfo) -> Unit,
    onCompleteUpdate: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is UpdateEvent.StartUpdateFlow -> onStartUpdateFlow(event.info)
            UpdateEvent.CompleteUpdate -> onCompleteUpdate()
        }
    }

    UpdateDialogs(prompt = state.prompt, onAction = viewModel::onAction)
}

@Composable
private fun UpdateDialogs(
    prompt: UpdatePrompt,
    onAction: (UpdateAction) -> Unit,
) {
    when (prompt) {
        is UpdatePrompt.UpdateAvailable -> {
            UpdateDialog(
                title = R.string.app_update_available_title,
                message = R.string.app_update_available_message,
                confirm = R.string.app_update_available_confirm,
                onConfirm = { onAction(UpdateAction.UpdateConfirmed) },
                onDismiss = { onAction(UpdateAction.UpdateDismissed) },
                testTag = "app_update_available_confirm",
            )
        }

        UpdatePrompt.ReadyToInstall -> {
            UpdateDialog(
                title = R.string.app_update_ready_title,
                message = R.string.app_update_ready_message,
                confirm = R.string.app_update_ready_confirm,
                onConfirm = { onAction(UpdateAction.InstallConfirmed) },
                onDismiss = { onAction(UpdateAction.InstallDismissed) },
                testTag = "app_update_ready_confirm",
            )
        }

        UpdatePrompt.None -> {
            Unit
        }
    }
}

@Composable
private fun UpdateDialog(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes confirm: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    testTag: String,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppThemeTokens.colors.surfaceRaised,
        title = { Text(stringResource(title)) },
        text = { Text(stringResource(message)) },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.testTag(testTag)) {
                Text(stringResource(confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.app_update_later)) }
        },
    )
}

@ComponentPreviews
@Composable
private fun ReadyToInstallDialogPreview() {
    AppTheme {
        UpdateDialogs(prompt = UpdatePrompt.ReadyToInstall, onAction = {})
    }
}
