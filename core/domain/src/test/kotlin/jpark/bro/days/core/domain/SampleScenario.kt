package jpark.bro.days.core.domain

import java.time.LocalDate

/** product-spec 9장 샘플 데이터 시나리오 */
internal object SampleScenario {
    val TODAY: LocalDate = LocalDate.of(2026, 11, 5)

    /** 지민 · 현우 사귄 날 (1일 세기) */
    val COUPLE_START: LocalDate = LocalDate.of(2025, 6, 14)
}
