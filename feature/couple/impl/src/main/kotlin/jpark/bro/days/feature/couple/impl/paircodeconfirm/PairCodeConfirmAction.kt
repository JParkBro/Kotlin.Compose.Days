package jpark.bro.days.feature.couple.impl.paircodeconfirm

import java.time.LocalDate

internal sealed interface PairCodeConfirmAction {
    data object StartDateClicked : PairCodeConfirmAction

    data class StartDateSelected(
        val date: LocalDate,
    ) : PairCodeConfirmAction

    data object DatePickerDismissed : PairCodeConfirmAction

    data class CountFirstDayChanged(
        val value: Boolean,
    ) : PairCodeConfirmAction

    data object ConnectClicked : PairCodeConfirmAction

    data object BackClicked : PairCodeConfirmAction
}
