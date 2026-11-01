package jpark.bro.days.feature.onboarding.impl.startmode

import jpark.bro.days.feature.onboarding.api.OnboardingResult
import jpark.bro.days.feature.onboarding.api.OnboardingResultHandler
import jpark.bro.days.feature.onboarding.impl.LoginKey
import jpark.bro.days.feature.onboarding.impl.StartModeKey
import jpark.bro.days.feature.onboarding.impl.TermsKey

internal sealed interface StartModeAction {
    data object SoloClicked : StartModeAction

    data object CoupleClicked : StartModeAction
}
