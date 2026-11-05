package jpark.bro.days.navigation

import jpark.bro.days.core.navigation.Navigator
import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.home.api.HomeKey
import jpark.bro.days.feature.onboarding.api.OnboardingKey
import jpark.bro.days.feature.onboarding.api.OnboardingResult
import org.junit.Assert.assertEquals
import org.junit.Test

class StartKeyTest {
    @Test
    fun `온보딩을 마쳤으면 홈에서 시작`() {
        assertEquals(HomeKey, startKeyFor(onboarded = true))
    }

    @Test
    fun `로그인이나 약관 동의를 마치지 않았으면 온보딩에서 시작`() {
        assertEquals(OnboardingKey, startKeyFor(onboarded = false))
    }

    @Test
    fun `커플로 시작하면 홈 위에 커플 연결을 열어 닫으면 홈으로 돌아감`() {
        val navigator = Navigator(startKey = OnboardingKey, topLevelKeys = setOf(HomeKey))

        AppOnboardingResultHandler(navigator).handle(OnboardingResult.Couple)
        assertEquals(listOf(HomeKey, PairingKey), navigator.backStack.toList())

        navigator.goBack()
        assertEquals(listOf(HomeKey), navigator.backStack.toList())
    }
}
