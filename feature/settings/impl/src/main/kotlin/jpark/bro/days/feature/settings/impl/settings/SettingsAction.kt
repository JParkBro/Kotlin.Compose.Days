package jpark.bro.days.feature.settings.impl.settings

import jpark.bro.days.feature.couple.api.CoupleManageKey
import jpark.bro.days.feature.settings.api.SettingsKey
import jpark.bro.days.feature.settings.impl.AccountKey
import jpark.bro.days.feature.settings.impl.LicenseDetailKey
import jpark.bro.days.feature.settings.impl.LicensesKey
import jpark.bro.days.feature.settings.impl.NotificationSettingsKey
import jpark.bro.days.feature.settings.impl.PolicyDocKey
import jpark.bro.days.feature.settings.impl.PolicyListKey
import jpark.bro.days.feature.settings.impl.PolicyType
import jpark.bro.days.feature.settings.impl.ProfileEditKey
import jpark.bro.days.feature.settings.impl.WidgetSettingsKey
import jpark.bro.days.feature.settings.impl.WithdrawKey

internal sealed interface SettingsAction {
    data class MenuClicked(
        val destination: SettingsDestination,
    ) : SettingsAction
}
