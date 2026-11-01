package jpark.bro.days.feature.onboarding.impl.terms

import jpark.bro.days.feature.onboarding.impl.LoginKey
import jpark.bro.days.feature.onboarding.impl.StartModeKey
import jpark.bro.days.feature.onboarding.impl.TermsKey

internal sealed interface TermsEvent {
    data object NavigateToStartMode : TermsEvent

    data object NavigateBack : TermsEvent
}
