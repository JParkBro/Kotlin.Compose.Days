package jpark.bro.days.core.model

import java.time.LocalDate

/**
 * 저장하지 않고 계산하는 자동 기념일 (product-spec 2-3).
 * ruleKey는 숨김(anniversary_hide)에 저장하는 키다.
 */
sealed interface AutoAnniversary {
    val date: LocalDate
    val ruleKey: String

    companion object {
        private const val DAY_PREFIX = "DAY_"
        private const val YEAR_PREFIX = "YEAR_"
        private const val BIRTHDAY_PREFIX = "BDAY_"

        /** [ruleKey]를 거꾸로 읽는다 (알림 목록처럼 날짜 없이 종류만 필요할 때). 형식이 다르면 null */
        fun ruleOf(ruleKey: String): AnniversaryRule? =
            when {
                ruleKey.startsWith(DAY_PREFIX) -> {
                    ruleKey.removePrefix(DAY_PREFIX).toIntOrNull()?.let(AnniversaryRule::Days)
                }

                ruleKey.startsWith(YEAR_PREFIX) -> {
                    ruleKey.removePrefix(YEAR_PREFIX).toIntOrNull()?.let(AnniversaryRule::Years)
                }

                // uid에 "_"가 들어 있어도 마지막 조각이 연도다
                ruleKey.startsWith(BIRTHDAY_PREFIX) -> {
                    ruleKey
                        .removePrefix(BIRTHDAY_PREFIX)
                        .substringBeforeLast("_", missingDelimiterValue = "")
                        .ifEmpty { null }
                        ?.let(AnniversaryRule::Birthday)
                }

                else -> {
                    null
                }
            }
    }

    /** 사귄 날 기준 100일 단위 */
    data class Days(
        val days: Int,
        override val date: LocalDate,
    ) : AutoAnniversary {
        override val ruleKey: String get() = "$DAY_PREFIX$days"
    }

    /** 사귄 날 기준 주년 */
    data class Years(
        val years: Int,
        override val date: LocalDate,
    ) : AutoAnniversary {
        override val ruleKey: String get() = "$YEAR_PREFIX$years"
    }

    /** 두 사람 프로필의 생일 */
    data class Birthday(
        val userId: String,
        val year: Int,
        override val date: LocalDate,
    ) : AutoAnniversary {
        override val ruleKey: String get() = "$BIRTHDAY_PREFIX${userId}_$year"
    }
}

/** [AutoAnniversary.ruleKey]에서 읽은 기념일 종류 */
sealed interface AnniversaryRule {
    data class Days(
        val days: Int,
    ) : AnniversaryRule

    data class Years(
        val years: Int,
    ) : AnniversaryRule

    data class Birthday(
        val userId: String,
    ) : AnniversaryRule
}
