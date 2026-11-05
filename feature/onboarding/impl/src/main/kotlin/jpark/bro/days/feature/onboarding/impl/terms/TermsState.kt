package jpark.bro.days.feature.onboarding.impl.terms

/** 약관 항목 (product-spec 5장) */
internal enum class Term(
    val required: Boolean,
) {
    AGE_OVER_14(required = true),
    SERVICE(required = true),
    PRIVACY(required = true),
    MARKETING(required = false),
}

internal data class TermsState(
    val checked: Set<Term> = emptySet(),
    val isSaving: Boolean = false,
) {
    val allChecked: Boolean get() = checked.containsAll(Term.entries)

    /** 필수 항목을 모두 체크해야 "동의하고 시작하기" 활성 */
    val canAgree: Boolean get() = !isSaving && Term.entries.filter { it.required }.all { it in checked }
}
