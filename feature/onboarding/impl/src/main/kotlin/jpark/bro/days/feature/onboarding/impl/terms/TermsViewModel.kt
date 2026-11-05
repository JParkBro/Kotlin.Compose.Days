package jpark.bro.days.feature.onboarding.impl.terms

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.OnboardingRepository
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.launch

internal class TermsViewModel(
    private val onboardingRepository: OnboardingRepository,
) : MviViewModel<TermsState, TermsAction, TermsEvent>(TermsState()) {
    override fun onAction(action: TermsAction) {
        when (action) {
            TermsAction.AllToggled -> {
                setState { copy(checked = if (allChecked) emptySet() else Term.entries.toSet()) }
            }

            is TermsAction.Toggled -> {
                setState {
                    copy(checked = if (action.term in checked) checked - action.term else checked + action.term)
                }
            }

            is TermsAction.ViewPolicyClicked -> {
                sendEvent(TermsEvent.OpenPolicy(action.type))
            }

            TermsAction.AgreeClicked -> {
                agree()
            }

            TermsAction.BackClicked -> {
                sendEvent(TermsEvent.NavigateBack)
            }
        }
    }

    private fun agree() {
        if (!currentState.canAgree) return
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            onboardingRepository
                .agreeTerms(marketing = Term.MARKETING in currentState.checked)
                .onSuccess { sendEvent(TermsEvent.NavigateToStartMode) }
                .onFailure { sendEvent(TermsEvent.AgreeFailed) }
            setState { copy(isSaving = false) }
        }
    }
}
