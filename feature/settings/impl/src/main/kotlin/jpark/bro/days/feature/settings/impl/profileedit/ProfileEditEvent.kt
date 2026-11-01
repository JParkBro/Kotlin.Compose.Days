package jpark.bro.days.feature.settings.impl.profileedit

internal sealed interface ProfileEditEvent {
    data object NavigateBack : ProfileEditEvent
}
