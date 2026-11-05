package jpark.bro.days.feature.onboarding.impl.startmode

internal sealed interface StartModeAction {
    data class ModeSelected(
        val mode: StartMode,
    ) : StartModeAction

    data object NextClicked : StartModeAction
}
