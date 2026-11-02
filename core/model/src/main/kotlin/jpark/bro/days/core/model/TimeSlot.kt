package jpark.bro.days.core.model

/** 일정 시간 (product-spec 2-4). EXACT일 때만 시각을 함께 저장한다 */
enum class TimeSlot {
    ALL_DAY,
    MORNING,
    LUNCH,
    EVENING,
    EXACT,
}
