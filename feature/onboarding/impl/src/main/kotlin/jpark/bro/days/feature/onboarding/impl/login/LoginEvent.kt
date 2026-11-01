package jpark.bro.days.feature.onboarding.impl.login

import jpark.bro.days.feature.onboarding.impl.LoginKey
import jpark.bro.days.feature.onboarding.impl.StartModeKey
import jpark.bro.days.feature.onboarding.impl.TermsKey

internal sealed interface LoginEvent {
    data object NavigateToTerms : LoginEvent
}
