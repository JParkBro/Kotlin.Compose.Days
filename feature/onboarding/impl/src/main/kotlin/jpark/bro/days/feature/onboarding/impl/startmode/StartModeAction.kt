package jpark.bro.days.feature.onboarding.impl.startmode

internal sealed interface StartModeAction {
    data object SoloClicked : StartModeAction

    data object CoupleClicked : StartModeAction
}
