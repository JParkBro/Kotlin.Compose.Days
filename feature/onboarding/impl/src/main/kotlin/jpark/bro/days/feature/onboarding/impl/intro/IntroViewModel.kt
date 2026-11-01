package jpark.bro.days.feature.onboarding.impl.intro

import jpark.bro.days.core.ui.MviViewModel

internal class IntroViewModel : MviViewModel<IntroState, IntroAction, IntroEvent>(IntroState()) {
    override fun onAction(action: IntroAction) {
        when (action) {
            IntroAction.StartClicked -> sendEvent(IntroEvent.NavigateToLogin)
        }
    }
}
