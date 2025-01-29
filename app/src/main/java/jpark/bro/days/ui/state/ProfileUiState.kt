package jpark.bro.days.ui.state

import jpark.bro.days.core.util.DateUtil
import kotlinx.datetime.LocalDate

data class ProfileUiState(
    val name: String = "",
    val birthday: LocalDate = DateUtil.getCurrentLocalDate(),
    val ageText: String = "",
)