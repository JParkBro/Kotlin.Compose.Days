package jpark.bro.days.feature.settings.impl

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 이 기능 안에서만 이동하는 화면 키 */
@Serializable
internal data object WidgetSettingsKey : NavKey

@Serializable
internal data object AccountKey : NavKey

@Serializable
internal data object WithdrawKey : NavKey

@Serializable
internal data object PolicyListKey : NavKey

/** 개발자 응원하기 (혜택 없는 후원 결제) */
@Serializable
internal data object SupportDeveloperKey : NavKey

/** 다른 앱 둘러보기 (크로스 프로모션 웹 페이지) */
@Serializable
internal data object MoreAppsKey : NavKey
