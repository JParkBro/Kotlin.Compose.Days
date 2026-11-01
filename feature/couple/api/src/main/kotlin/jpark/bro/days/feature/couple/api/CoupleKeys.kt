package jpark.bro.days.feature.couple.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 커플 연결 (내 코드 · 코드 입력 탭) */
@Serializable
data object PairingKey : NavKey

/** 커플 관리 (설정 → 커플) */
@Serializable
data object CoupleManageKey : NavKey
