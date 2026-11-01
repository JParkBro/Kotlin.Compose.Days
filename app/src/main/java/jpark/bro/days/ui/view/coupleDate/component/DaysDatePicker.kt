package jpark.bro.days.ui.view.coupleDate.component

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import jpark.bro.days.R
import jpark.bro.days.core.util.DateUtil
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaysDatePicker(
    initialDate: LocalDate = DateUtil.getCurrentLocalDate(),
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = initialDate.atStartOfDayIn(TimeZone.of("UTC")).toEpochMilliseconds(),
        )

    val selectedDate =
        datePickerState.selectedDateMillis?.let {
            Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).date
        } ?: initialDate

    val handleConfirmClick: () -> Unit = {
        onDateSelected(selectedDate)
        onDismiss()
    }
    val handleDismissClick: () -> Unit = {
        onDismiss()
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = handleConfirmClick) {
                Text(text = stringResource(R.string.en_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = handleDismissClick) {
                Text(text = stringResource(R.string.en_cancel))
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false,
        )
    }
}
