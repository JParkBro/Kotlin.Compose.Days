package jpark.bro.days.core.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigatorTest {
    private data object Home : NavKey

    private data object Calendar : NavKey

    private data object Settings : NavKey

    private data object Onboarding : NavKey

    private data class Detail(
        val id: String,
    ) : NavKey

    private fun tabs() = Navigator(startKey = Home, topLevelKeys = setOf(Home, Calendar, Settings))

    @Test
    fun `시작하면 시작 키만 있다`() {
        val navigator = tabs()

        assertEquals(listOf(Home), navigator.backStack)
        assertEquals(Home, navigator.currentTab)
    }

    @Test
    fun `탭이 아닌 키는 현재 탭 스택에 쌓인다`() {
        val navigator = tabs()

        navigator.goTo(Detail("1"))

        assertEquals(listOf(Home, Detail("1")), navigator.backStack)
    }

    @Test
    fun `다른 탭으로 전환하면 시작 탭 스택 위에 그 탭 스택이 보인다`() {
        val navigator = tabs()

        navigator.goTo(Calendar)
        navigator.goTo(Detail("c"))

        assertEquals(Calendar, navigator.currentTab)
        assertEquals(listOf(Home, Calendar, Detail("c")), navigator.backStack)
    }

    @Test
    fun `탭을 바꿔도 각 탭의 화면 위치가 유지된다`() {
        val navigator = tabs()
        navigator.goTo(Detail("h"))
        navigator.goTo(Calendar)
        navigator.goTo(Detail("c"))

        navigator.goTo(Home)
        assertEquals(listOf(Home, Detail("h")), navigator.backStack)

        navigator.goTo(Calendar)
        assertEquals(listOf(Home, Detail("h"), Calendar, Detail("c")), navigator.backStack)
    }

    @Test
    fun `탭 첫 화면에서 뒤로 가면 시작 탭으로 돌아간다`() {
        val navigator = tabs()
        navigator.goTo(Settings)

        navigator.goBack()

        assertEquals(Home, navigator.currentTab)
        assertEquals(listOf(Home), navigator.backStack)
    }

    @Test
    fun `시작 탭 첫 화면에서는 뒤로 가도 그대로`() {
        val navigator = tabs()

        navigator.goBack()

        assertEquals(listOf(Home), navigator.backStack)
    }

    @Test
    fun `replaceAll은 모든 탭 스택을 지우고 새 시작점이 된다`() {
        val navigator = tabs()
        navigator.goTo(Detail("h"))
        navigator.goTo(Calendar)

        navigator.replaceAll(Onboarding)

        assertEquals(listOf(Onboarding), navigator.backStack)
        assertEquals(Onboarding, navigator.currentTab)
    }

    @Test
    fun `온보딩 완료 후 replaceAll(홈)이면 홈이 시작 탭`() {
        val navigator = Navigator(startKey = Onboarding, topLevelKeys = setOf(Home, Calendar, Settings))

        navigator.replaceAll(Home)
        navigator.goTo(Calendar)
        navigator.goBack()

        assertEquals(listOf(Home), navigator.backStack)
    }

    @Test
    fun `replaceTop은 현재 화면만 바꾼다`() {
        val navigator = tabs()
        navigator.goTo(Detail("1"))

        navigator.replaceTop(Detail("2"))

        assertEquals(listOf(Home, Detail("2")), navigator.backStack)
    }
}
