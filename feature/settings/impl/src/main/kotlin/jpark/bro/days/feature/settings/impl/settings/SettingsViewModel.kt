package jpark.bro.days.feature.settings.impl.settings

import jpark.bro.days.core.ui.MviViewModel

internal class SettingsViewModel : MviViewModel<SettingsState, SettingsAction, SettingsEvent>(SettingsState()) {
    override fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.MenuClicked -> sendEvent(SettingsEvent.Navigate(action.destination))
        }
    }
}
