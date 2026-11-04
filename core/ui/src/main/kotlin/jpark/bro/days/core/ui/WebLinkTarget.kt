package jpark.bro.days.core.ui

/** 웹뷰 안의 링크를 어디서 열지 */
enum class WebLinkTarget {
    /** 처음 연 페이지와 같은 사이트: 웹뷰 안에서 */
    IN_PAGE,

    /** Play 스토어: 스토어 앱으로 넘긴다 */
    PLAY_STORE,

    /** 그 밖의 사이트: 열지 않는다 */
    BLOCKED,
}

/** [pageHost] 페이지에서 누른 링크(scheme · host)를 어디서 열지 고른다 */
fun webLinkTarget(
    pageHost: String,
    linkScheme: String?,
    linkHost: String?,
): WebLinkTarget =
    when {
        linkScheme == "https" && linkHost == pageHost -> WebLinkTarget.IN_PAGE
        linkScheme == "https" && linkHost == PLAY_STORE_HOST -> WebLinkTarget.PLAY_STORE
        linkScheme == "market" -> WebLinkTarget.PLAY_STORE
        else -> WebLinkTarget.BLOCKED
    }

private const val PLAY_STORE_HOST = "play.google.com"
