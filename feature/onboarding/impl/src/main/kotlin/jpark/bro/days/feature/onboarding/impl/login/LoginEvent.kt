package jpark.bro.days.feature.onboarding.impl.login

internal sealed interface LoginEvent {
    data object NavigateToTerms : LoginEvent
}
