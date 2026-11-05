package jpark.bro.days.feature.onboarding.impl.terms

import jpark.bro.days.feature.settings.api.PolicyType

internal sealed interface TermsAction {
    /** 전체 동의: 모두 체크돼 있으면 모두 풀고, 아니면 모두 체크 */
    data object AllToggled : TermsAction

    data class Toggled(
        val term: Term,
    ) : TermsAction

    data class ViewPolicyClicked(
        val type: PolicyType,
    ) : TermsAction

    data object AgreeClicked : TermsAction

    data object BackClicked : TermsAction
}
