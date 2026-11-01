package jpark.bro.days.feature.settings.impl.profileedit

internal sealed interface ProfileEditAction {
    data object BackClicked : ProfileEditAction
}
