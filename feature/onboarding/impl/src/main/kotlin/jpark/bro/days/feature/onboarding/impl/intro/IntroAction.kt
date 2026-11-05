package jpark.bro.days.feature.onboarding.impl.intro

internal sealed interface IntroAction {
    /** 마지막 장의 시작하기 · 건너뛰기 */
    data object StartClicked : IntroAction
}
