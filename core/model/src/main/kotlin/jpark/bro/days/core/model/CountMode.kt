package jpark.bro.days.core.model

/** 디데이 계산 방식 (product-spec 2-2) */
enum class CountMode {
    /** 남은 날 카운트다운 (D-N) */
    DDAY,

    /** 지난 날 카운트 (D+N) */
    DPLUS,
}
