package jpark.bro.days.navigation

import jpark.bro.days.feature.home.api.HomeKey
import jpark.bro.days.feature.onboarding.api.OnboardingKey
import org.junit.Assert.assertEquals
import org.junit.Test

class StartKeyTest {
    @Test
    fun `로그인했으면 홈에서 시작`() {
        assertEquals(HomeKey, startKeyFor(signedIn = true))
    }

    @Test
    fun `로그인하지 않았으면 온보딩에서 시작`() {
        assertEquals(OnboardingKey, startKeyFor(signedIn = false))
    }
}
