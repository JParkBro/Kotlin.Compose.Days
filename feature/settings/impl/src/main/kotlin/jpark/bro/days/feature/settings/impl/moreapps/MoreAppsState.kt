package jpark.bro.days.feature.settings.impl.moreapps

internal data class MoreAppsState(
    val isLoading: Boolean = true,
    val isFailed: Boolean = false,
    /** 바뀌면 웹뷰가 페이지를 다시 불러온다 */
    val reloadCount: Int = 0,
)
