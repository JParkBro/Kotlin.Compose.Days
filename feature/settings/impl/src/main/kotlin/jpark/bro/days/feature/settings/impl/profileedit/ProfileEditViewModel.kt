package jpark.bro.days.feature.settings.impl.profileedit

import jpark.bro.days.core.ui.MviViewModel

internal class ProfileEditViewModel :
    MviViewModel<ProfileEditState, ProfileEditAction, ProfileEditEvent>(ProfileEditState()) {
    override fun onAction(action: ProfileEditAction) {
        when (action) {
            ProfileEditAction.BackClicked -> sendEvent(ProfileEditEvent.NavigateBack)
        }
    }
}
