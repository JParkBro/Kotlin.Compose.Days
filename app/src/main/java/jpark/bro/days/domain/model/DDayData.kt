package jpark.bro.days.domain.model

import kotlinx.datetime.LocalDate

data class DDayData(
    val label: String,
    val date: LocalDate,
    val dDayCount: Int,
    val isABirthday: Boolean = false,
    val isBBirthday: Boolean = false,
)
