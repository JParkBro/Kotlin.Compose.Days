package jpark.bro.days.feature.settings.impl.widgetsettings

import jpark.bro.days.core.ui.MviViewModel

internal class WidgetSettingsViewModel :
    MviViewModel<WidgetSettingsState, WidgetSettingsAction, WidgetSettingsEvent>(WidgetSettingsState()) {
    override fun onAction(action: WidgetSettingsAction) {
        when (action) {
            WidgetSettingsAction.BackClicked -> sendEvent(WidgetSettingsEvent.NavigateBack)
        }
    }
}
