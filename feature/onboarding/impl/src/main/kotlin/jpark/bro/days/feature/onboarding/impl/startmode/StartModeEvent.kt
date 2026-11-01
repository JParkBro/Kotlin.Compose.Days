package jpark.bro.days.feature.onboarding.impl.startmode

import jpark.bro.days.feature.onboarding.api.OnboardingResult

internal sealed interface StartModeEvent {
    data class Finished(
        val result: OnboardingResult,
    ) : StartModeEvent
}
