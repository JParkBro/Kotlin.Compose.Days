package jpark.bro.days.core.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import jpark.bro.days.core.model.Visibility

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

/** 공개 범위 이름 (편집 · 홈 · 상세 공용) */
@get:StringRes
val Visibility.label: Int
    get() =
        when (this) {
            Visibility.SHARED -> R.string.common_visibility_shared
            Visibility.PRIVATE -> R.string.common_visibility_private
        }

/** 자동 기념일 이름: "600일", "2주년", "지민 생일". [nicknameOf]가 null을 주면 이름 없이 "생일" */
@Composable
fun AutoAnniversary.title(nicknameOf: (userId: String) -> String?): String =
    when (this) {
        is AutoAnniversary.Days -> {
            stringResource(R.string.common_anniversary_days, days)
        }

        is AutoAnniversary.Years -> {
            stringResource(R.string.common_anniversary_years, years)
        }

        is AutoAnniversary.Birthday -> {
            nicknameOf(userId)?.let { stringResource(R.string.common_anniversary_birthday, it) }
                ?: stringResource(R.string.common_anniversary_birthday_unknown)
        }
    }
