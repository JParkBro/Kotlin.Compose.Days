package jpark.bro.days.feature.settings.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SettingsKey : NavKey

/** 프로필 수정 (커플 코드 발급에 이름이 필요해 커플 연결 화면에서도 연다) */
@Serializable
data object ProfileEditKey : NavKey

/** 알림 설정. 알림 목록의 "알림 설정 바로가기"에서도 연다 */
@Serializable
data object NotificationSettingsKey : NavKey
