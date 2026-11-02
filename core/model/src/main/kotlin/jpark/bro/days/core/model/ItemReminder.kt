package jpark.bro.days.core.model

import java.time.LocalTime

/** 항목 알림 (사용자별, 공유하지 않음). [offsetDays] 0 = 당일, 1 = 하루 전, 7 = 일주일 전 */
data class ItemReminder(
    val offsetDays: Int,
    val timeOfDay: LocalTime,
)
