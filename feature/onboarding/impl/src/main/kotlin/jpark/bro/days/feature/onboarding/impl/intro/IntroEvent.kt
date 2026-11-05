package jpark.bro.days.feature.onboarding.impl.intro

internal sealed interface IntroEvent {
    /** 소개를 로그인으로 바꾼다 (뒤로 가도 소개로 돌아오지 않음) */
    data object NavigateToLogin : IntroEvent
}
