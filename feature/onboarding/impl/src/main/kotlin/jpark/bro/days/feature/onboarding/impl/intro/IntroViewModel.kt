package jpark.bro.days.feature.onboarding.impl.intro

import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.onboarding.api.OnboardingKey
import jpark.bro.days.feature.onboarding.impl.LoginKey
import jpark.bro.days.feature.onboarding.impl.StartModeKey
import jpark.bro.days.feature.onboarding.impl.TermsKey

internal class IntroViewModel : MviViewModel<IntroState, IntroAction, IntroEvent>(IntroState()) {
    override fun onAction(action: IntroAction) {
        when (action) {
            IntroAction.StartClicked -> sendEvent(IntroEvent.NavigateToLogin)
        }
    }
}
