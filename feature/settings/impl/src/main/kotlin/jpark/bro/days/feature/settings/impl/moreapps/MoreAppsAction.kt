package jpark.bro.days.feature.settings.impl.moreapps

internal sealed interface MoreAppsAction {
    data object PageLoaded : MoreAppsAction

    data object PageFailed : MoreAppsAction

    data object RetryClicked : MoreAppsAction

    /** 페이지의 Play 스토어 링크 */
    data class StoreLinkClicked(
        val url: String,
    ) : MoreAppsAction

    data object BackClicked : MoreAppsAction
}
