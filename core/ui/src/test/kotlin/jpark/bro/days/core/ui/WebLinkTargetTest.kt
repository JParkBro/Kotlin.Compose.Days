package jpark.bro.days.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class WebLinkTargetTest {
    private val page = "jparkbro-apps.jparkbro.workers.dev"

    @Test
    fun `같은 사이트 https 링크는 웹뷰 안에서 열기`() {
        assertEquals(WebLinkTarget.IN_PAGE, webLinkTarget(page, "https", page))
    }

    @Test
    fun `Play 스토어 링크는 스토어 앱으로`() {
        assertEquals(WebLinkTarget.PLAY_STORE, webLinkTarget(page, "https", "play.google.com"))
        assertEquals(WebLinkTarget.PLAY_STORE, webLinkTarget(page, "market", "details"))
    }

    @Test
    fun `다른 사이트 · http · 다른 스킴은 막음`() {
        assertEquals(WebLinkTarget.BLOCKED, webLinkTarget(page, "https", "example.com"))
        assertEquals(WebLinkTarget.BLOCKED, webLinkTarget(page, "http", page))
        assertEquals(WebLinkTarget.BLOCKED, webLinkTarget(page, "intent", null))
        assertEquals(WebLinkTarget.BLOCKED, webLinkTarget(page, "javascript", null))
    }
}
