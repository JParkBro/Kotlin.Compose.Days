package jpark.bro.days.feature.couple.impl.anniversaries

import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.DdayCount

internal data class AnniversaryListState(
    val isLoading: Boolean = true,
    /** 오늘부터 1년 안의 자동 기념일 (숨긴 것도 포함, 날짜순) */
    val rows: List<AnniversaryRow> = emptyList(),
    /** 생일 기념일 이름에 쓰는 userId → 이름 */
    val nicknames: Map<String, String> = emptyMap(),
)

internal data class AnniversaryRow(
    val anniversary: AutoAnniversary,
    val dday: DdayCount,
    val isHidden: Boolean,
)
