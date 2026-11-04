package jpark.bro.days.feature.couple.impl.couplemanage

import java.time.LocalDate

internal sealed interface CoupleManageAction {
    data object StartDateClicked : CoupleManageAction

    data class StartDateSelected(
        val date: LocalDate,
    ) : CoupleManageAction

    data object DatePickerDismissed : CoupleManageAction

    data class CountFirstDayChanged(
        val value: Boolean,
    ) : CoupleManageAction

    data object UnpairClicked : CoupleManageAction

    data object UnpairDismissed : CoupleManageAction

    data class DeleteCopiesChanged(
        val value: Boolean,
    ) : CoupleManageAction

    data object UnpairConfirmed : CoupleManageAction

    data object BackClicked : CoupleManageAction
}
