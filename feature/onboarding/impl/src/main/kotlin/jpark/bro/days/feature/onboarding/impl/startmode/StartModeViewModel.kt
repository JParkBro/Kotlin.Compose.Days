package jpark.bro.days.feature.onboarding.impl.startmode

import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.onboarding.api.OnboardingResult

internal class StartModeViewModel : MviViewModel<StartModeState, StartModeAction, StartModeEvent>(StartModeState()) {
    override fun onAction(action: StartModeAction) {
        when (action) {
            is StartModeAction.ModeSelected -> {
                setState { copy(selected = action.mode) }
            }

            StartModeAction.NextClicked -> {
                val result =
                    when (currentState.selected) {
                        StartMode.SOLO -> OnboardingResult.Solo
                        StartMode.COUPLE -> OnboardingResult.Couple
                    }
                sendEvent(StartModeEvent.Finished(result))
            }
        }
    }
}
