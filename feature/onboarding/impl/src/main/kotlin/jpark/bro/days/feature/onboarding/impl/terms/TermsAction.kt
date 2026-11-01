package jpark.bro.days.feature.onboarding.impl.terms

internal sealed interface TermsAction {
    data object AgreeClicked : TermsAction

    data object BackClicked : TermsAction
}
