package jpark.bro.days.core.domain.dday

import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.TimeSlot
import java.time.LocalTime

private const val MORNING_HOUR = 9
private const val LUNCH_HOUR = 12
private const val EVENING_HOUR = 18

/**
 * 같은 날 일정을 정렬할 때 쓰는 시각 (홈 · 캘린더 공용).
 * 종일 · 시간 없음이 맨 앞, 시간대는 대표 시각(아침 9시 · 점심 12시 · 저녁 6시), 시간 지정은 그 시각.
 */
val ItemContent.sortTime: LocalTime
    get() =
        when (timeSlot) {
            null, TimeSlot.ALL_DAY -> LocalTime.MIN
            TimeSlot.MORNING -> LocalTime.of(MORNING_HOUR, 0)
            TimeSlot.LUNCH -> LocalTime.of(LUNCH_HOUR, 0)
            TimeSlot.EVENING -> LocalTime.of(EVENING_HOUR, 0)
            TimeSlot.EXACT -> time ?: LocalTime.MIN
        }
