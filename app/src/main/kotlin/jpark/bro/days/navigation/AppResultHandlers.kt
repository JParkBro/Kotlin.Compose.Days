package jpark.bro.days.navigation

import androidx.navigation3.runtime.NavKey
import jpark.bro.days.core.navigation.Navigator
import jpark.bro.days.feature.couple.api.PairingKey
import jpark.bro.days.feature.home.api.HomeKey
import jpark.bro.days.feature.onboarding.api.OnboardingKey
import jpark.bro.days.feature.onboarding.api.OnboardingResult
import jpark.bro.days.feature.onboarding.api.OnboardingResultHandler
import jpark.bro.days.feature.settings.api.SessionEnded
import jpark.bro.days.feature.settings.api.SessionEndedHandler

/** 앱을 켰을 때 첫 화면: 온보딩(로그인 · 약관 동의)을 마쳤으면 홈, 아니면 온보딩 */
internal fun startKeyFor(onboarded: Boolean): NavKey = if (onboarded) HomeKey else OnboardingKey

/** 온보딩 완료 후 목적지 (module-map 4-3) */
internal class AppOnboardingResultHandler(
    private val navigator: Navigator,
) : OnboardingResultHandler {
    override fun handle(result: OnboardingResult) {
        when (result) {
            OnboardingResult.Solo -> {
                navigator.replaceAll(HomeKey)
            }

            // 커플 연결을 닫으면 홈으로 돌아오게 홈 위에 연다
            OnboardingResult.Couple -> {
                navigator.replaceAll(HomeKey)
                navigator.goTo(PairingKey)
            }
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
