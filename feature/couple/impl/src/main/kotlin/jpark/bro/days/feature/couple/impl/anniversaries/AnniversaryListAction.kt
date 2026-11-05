package jpark.bro.days.feature.couple.impl.anniversaries

internal sealed interface AnniversaryListAction {
    /** [visible]이 false면 숨긴다 */
    data class VisibilityChanged(
        val ruleKey: String,
        val visible: Boolean,
    ) : AnniversaryListAction

    data object BackClicked : AnniversaryListAction
}
