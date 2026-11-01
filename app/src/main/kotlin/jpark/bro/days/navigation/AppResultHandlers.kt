package jpark.bro.days.navigation

import jpark.bro.days.core.navigation.Navigator
import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.home.api.HomeKey
import jpark.bro.days.feature.onboarding.api.OnboardingKey
import jpark.bro.days.feature.onboarding.api.OnboardingResult
import jpark.bro.days.feature.onboarding.api.OnboardingResultHandler
import jpark.bro.days.feature.settings.api.SessionEnded
import jpark.bro.days.feature.settings.api.SessionEndedHandler

/** 온보딩 완료 후 목적지 (module-map 4-3) */
internal class AppOnboardingResultHandler(
    private val navigator: Navigator,
) : OnboardingResultHandler {
    override fun handle(result: OnboardingResult) {
        when (result) {
            OnboardingResult.Solo -> navigator.replaceAll(HomeKey)
            OnboardingResult.Couple -> navigator.replaceAll(PairingKey)
        }
    }
}

/** 로그아웃 · 탈퇴 후 온보딩으로 (module-map 4-3) */
internal class AppSessionEndedHandler(
    private val navigator: Navigator,
) : SessionEndedHandler {
    override fun handle(result: SessionEnded) {
        navigator.replaceAll(OnboardingKey)
    }
}
