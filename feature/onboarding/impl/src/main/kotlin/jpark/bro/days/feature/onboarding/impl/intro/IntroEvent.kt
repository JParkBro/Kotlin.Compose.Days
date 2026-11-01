package jpark.bro.days.feature.onboarding.impl.intro

import jpark.bro.days.feature.onboarding.api.OnboardingKey
import jpark.bro.days.feature.onboarding.impl.LoginKey
import jpark.bro.days.feature.onboarding.impl.StartModeKey
import jpark.bro.days.feature.onboarding.impl.TermsKey

internal sealed interface IntroEvent {
    data object NavigateToLogin : IntroEvent
}
