package jpark.bro.days.core.model

/** 화면에 표시할 디데이 값 */
sealed interface DdayCount {
    /** D-N */
    data class Remaining(
        val days: Int,
    ) : DdayCount

    /** D-Day (당일) */
    data object Today : DdayCount

    /** D+N */
    data class Elapsed(
        val days: Int,
    ) : DdayCount
}
