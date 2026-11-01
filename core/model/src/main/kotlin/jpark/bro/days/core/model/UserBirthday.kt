package jpark.bro.days.core.model

import java.time.LocalDate

/** 자동 기념일(생일) 계산에 쓰는 프로필 생일 */
data class UserBirthday(
    val userId: String,
    val birthDate: LocalDate,
)
