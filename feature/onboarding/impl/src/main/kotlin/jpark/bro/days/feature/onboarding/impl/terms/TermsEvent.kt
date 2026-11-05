package jpark.bro.days.feature.onboarding.impl.terms

import jpark.bro.days.feature.settings.api.PolicyType

internal sealed interface TermsEvent {
    data object NavigateToStartMode : TermsEvent

    data object NavigateBack : TermsEvent

    data class OpenPolicy(
        val type: PolicyType,
    ) : TermsEvent

    data object AgreeFailed : TermsEvent
}
