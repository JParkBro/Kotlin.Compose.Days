package jpark.bro.days.feature.onboarding.impl.terms

internal sealed interface TermsEvent {
    data object NavigateToStartMode : TermsEvent

    data object NavigateBack : TermsEvent
}
