package jpark.bro.days.feature.onboarding.impl.intro

internal data class IntroState(
    /** 이 기기에서 소개를 본 적 있는지 확인하는 동안은 빈 화면 (봤으면 바로 로그인으로) */
    val isLoading: Boolean = true,
)
