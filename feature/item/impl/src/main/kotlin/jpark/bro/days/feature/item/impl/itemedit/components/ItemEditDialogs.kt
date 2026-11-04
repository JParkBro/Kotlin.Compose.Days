package jpark.bro.days.feature.item.impl.itemedit.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.feature.item.impl.R
import java.time.LocalTime
import jpark.bro.days.core.designsystem.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ItemTimePickerDialog(
    initial: LocalTime?,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val start = initial ?: DefaultExactTime
    val state = rememberTimePickerState(initialHour = start.hour, initialMinute = start.minute, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppThemeTokens.colors.surfaceRaised,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text(stringResource(DesignR.string.common_confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(DesignR.string.common_cancel)) } },
        text = { TimePicker(state = state) },
    )
}

@Composable
internal fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppThemeTokens.colors.surfaceRaised,
        title = { Text(stringResource(R.string.item_edit_delete_confirm_title)) },
        text = { Text(stringResource(R.string.item_edit_delete_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(DesignR.string.common_delete), color = AppThemeTokens.colors.danger)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(DesignR.string.common_cancel)) } },
    )
}

/** 시간 지정을 처음 고를 때 시계 바늘 위치 (저녁 약속이 흔해서 오후 7시) */
private const val DEFAULT_EXACT_HOUR = 19
private val DefaultExactTime: LocalTime = LocalTime.of(DEFAULT_EXACT_HOUR, 0)
