package jpark.bro.days.feature.item.impl.itemedit.components

import androidx.annotation.StringRes
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.Category
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import jpark.bro.days.feature.item.impl.R

@get:StringRes
internal val Category.label: Int
    get() =
        when (this) {
            Category.ANNIVERSARY -> R.string.item_edit_category_anniversary
            Category.BIRTHDAY -> R.string.item_edit_category_birthday
            Category.TRAVEL -> R.string.item_edit_category_travel
            Category.GOAL -> R.string.item_edit_category_goal
            Category.DAILY -> R.string.item_edit_category_daily
            Category.ETC -> R.string.item_edit_category_etc
        }

@get:StringRes
internal val ItemIcon.label: Int
    get() =
        when (this) {
            ItemIcon.HEART -> R.string.item_edit_icon_heart
            ItemIcon.CAKE -> R.string.item_edit_icon_cake
            ItemIcon.FLIGHT -> R.string.item_edit_icon_flight
            ItemIcon.SCHOOL -> R.string.item_edit_icon_school
            ItemIcon.STAR -> R.string.item_edit_icon_star
            ItemIcon.EVENT -> R.string.item_edit_icon_event
            ItemIcon.GIFT -> R.string.item_edit_icon_gift
            ItemIcon.FLAG -> R.string.item_edit_icon_flag
        }

@get:StringRes
internal val AppThemeType.colorLabel: Int
    get() =
        when (this) {
            AppThemeType.PINK -> R.string.item_edit_color_pink
            AppThemeType.NAVY -> R.string.item_edit_color_navy
            AppThemeType.SAGE -> R.string.item_edit_color_sage
            AppThemeType.LAVENDER -> R.string.item_edit_color_lavender
        }

@get:StringRes
internal val RepeatRule.label: Int
    get() =
        when (this) {
            RepeatRule.NONE -> R.string.item_edit_repeat_none
            RepeatRule.YEARLY -> R.string.item_edit_repeat_yearly
            RepeatRule.MONTHLY -> R.string.item_edit_repeat_monthly
        }

@get:StringRes
internal val TimeSlot.label: Int
    get() =
        when (this) {
            TimeSlot.ALL_DAY -> R.string.item_edit_time_all_day
            TimeSlot.MORNING -> R.string.item_edit_time_morning
            TimeSlot.LUNCH -> R.string.item_edit_time_lunch
            TimeSlot.EVENING -> R.string.item_edit_time_evening
            TimeSlot.EXACT -> R.string.item_edit_time_exact
        }
