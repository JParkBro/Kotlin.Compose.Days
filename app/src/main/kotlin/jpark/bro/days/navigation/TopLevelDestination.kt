package jpark.bro.days.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import jpark.bro.days.R
import jpark.bro.days.feature.calendar.api.CalendarKey
import jpark.bro.days.feature.home.api.HomeKey
import jpark.bro.days.feature.settings.api.SettingsKey

/** 하단 탭 3개 (product-spec 3-1) */
internal enum class TopLevelDestination(
    val key: NavKey,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) {
    HOME(HomeKey, R.string.app_tab_home, R.drawable.ic_tab_home),
    CALENDAR(CalendarKey, R.string.app_tab_calendar, R.drawable.ic_tab_calendar),
    SETTINGS(SettingsKey, R.string.app_tab_settings, R.drawable.ic_tab_settings),
    ;

    companion object {
        val keys: Set<NavKey> = entries.map { it.key }.toSet()
    }
}
