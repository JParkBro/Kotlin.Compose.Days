package jpark.bro.days.feature.settings.impl.moreapps

internal sealed interface MoreAppsEvent {
    /** Play 스토어 앱(없으면 브라우저)으로 연다 */
    data class OpenStore(
        val url: String,
    ) : MoreAppsEvent

    data object NavigateBack : MoreAppsEvent
}
