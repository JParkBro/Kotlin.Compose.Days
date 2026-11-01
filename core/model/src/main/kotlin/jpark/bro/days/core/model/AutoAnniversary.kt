package jpark.bro.days.core.model

import java.time.LocalDate

/**
 * 저장하지 않고 계산하는 자동 기념일 (product-spec 2-3).
 * ruleKey는 숨김(anniversary_hide)에 저장하는 키다.
 */
sealed interface AutoAnniversary {
    val date: LocalDate
    val ruleKey: String

    /** 사귄 날 기준 100일 단위 */
    data class Days(
        val days: Int,
        override val date: LocalDate,
    ) : AutoAnniversary {
        override val ruleKey: String get() = "DAY_$days"
    }

    /** 사귄 날 기준 주년 */
    data class Years(
        val years: Int,
        override val date: LocalDate,
    ) : AutoAnniversary {
        override val ruleKey: String get() = "YEAR_$years"
    }

    /** 두 사람 프로필의 생일 */
    data class Birthday(
        val userId: String,
        val year: Int,
        override val date: LocalDate,
    ) : AutoAnniversary {
        override val ruleKey: String get() = "BDAY_${userId}_$year"
    }
}
