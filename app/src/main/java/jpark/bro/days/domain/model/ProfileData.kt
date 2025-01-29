package jpark.bro.days.domain.model

import kotlinx.datetime.LocalDate

data class ProfileData(
    val nickname: String?,
    val birthday: LocalDate,
)
