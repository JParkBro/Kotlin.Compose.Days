package jpark.bro.days.feature.onboarding.impl.intro

internal sealed interface IntroEvent {
    data object NavigateToLogin : IntroEvent
}
