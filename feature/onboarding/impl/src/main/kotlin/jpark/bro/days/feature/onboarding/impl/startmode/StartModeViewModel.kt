package jpark.bro.days.feature.onboarding.impl.startmode

import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.onboarding.api.OnboardingResult
import jpark.bro.days.feature.onboarding.api.OnboardingResultHandler
import jpark.bro.days.feature.onboarding.impl.LoginKey
import jpark.bro.days.feature.onboarding.impl.StartModeKey
import jpark.bro.days.feature.onboarding.impl.TermsKey

internal class StartModeViewModel : MviViewModel<StartModeState, StartModeAction, StartModeEvent>(StartModeState()) {
    override fun onAction(action: StartModeAction) {
        when (action) {
            StartModeAction.SoloClicked -> sendEvent(StartModeEvent.Finished(OnboardingResult.Solo))
            StartModeAction.CoupleClicked -> sendEvent(StartModeEvent.Finished(OnboardingResult.Couple))
        }
    }
}
