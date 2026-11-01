package jpark.bro.days.feature.settings.impl.policydoc

import jpark.bro.days.core.ui.MviViewModel
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
import jpark.bro.days.feature.settings.impl.settings.SettingsDestination

internal class PolicyDocViewModel(
    key: PolicyDocKey,
) : MviViewModel<PolicyDocState, PolicyDocAction, Nothing>(PolicyDocState(type = key.type)) {
    override fun onAction(action: PolicyDocAction) {
        when (action) {
            PolicyDocAction.Reload -> setState { copy(isLoading = true) }
        }
    }
}
