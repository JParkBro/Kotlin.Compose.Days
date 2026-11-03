package jpark.bro.days.feature.onboarding.impl

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 이 기능 안에서만 이동하는 화면 키 */
@Serializable
internal data object IntroKey : NavKey

@Serializable
internal data object LoginKey : NavKey

@Serializable
internal data object TermsKey : NavKey

@Serializable
internal data object StartModeKey : NavKey
