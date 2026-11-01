package jpark.bro.days.feature.onboarding.impl.terms

import jpark.bro.days.core.ui.MviViewModel

internal class TermsViewModel : MviViewModel<TermsState, TermsAction, TermsEvent>(TermsState()) {
    override fun onAction(action: TermsAction) {
        when (action) {
            TermsAction.AgreeClicked -> sendEvent(TermsEvent.NavigateToStartMode)
            TermsAction.BackClicked -> sendEvent(TermsEvent.NavigateBack)
        }
    }
}
