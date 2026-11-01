package jpark.bro.days.feature.onboarding.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 온보딩 시작(소개 화면). 끝나면 OnboardingResult를 알리고 app이 목적지를 정한다 (module-map 4-3). */
@Serializable
data object OnboardingKey : NavKey
