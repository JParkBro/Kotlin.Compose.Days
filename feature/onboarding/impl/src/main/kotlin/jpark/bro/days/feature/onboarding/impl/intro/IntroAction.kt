package jpark.bro.days.feature.onboarding.impl.intro

internal sealed interface IntroAction {
    data object StartClicked : IntroAction
}
