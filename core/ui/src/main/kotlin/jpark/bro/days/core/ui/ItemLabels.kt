package jpark.bro.days.core.ui

import androidx.annotation.StringRes
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot

/** 일정 시간 이름 (편집 · 홈 · 캘린더 공용) */
@get:StringRes
val TimeSlot.label: Int
    get() =
        when (this) {
            TimeSlot.ALL_DAY -> R.string.common_time_all_day
            TimeSlot.MORNING -> R.string.common_time_morning
            TimeSlot.LUNCH -> R.string.common_time_lunch
            TimeSlot.EVENING -> R.string.common_time_evening
            TimeSlot.EXACT -> R.string.common_time_exact
        }

/** 반복 이름 (편집 · 홈 · 상세 공용) */
@get:StringRes
val RepeatRule.label: Int
    get() =
        when (this) {
            RepeatRule.NONE -> R.string.common_repeat_none
            RepeatRule.YEARLY -> R.string.common_repeat_yearly
            RepeatRule.MONTHLY -> R.string.common_repeat_monthly
        }
