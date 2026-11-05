package jpark.bro.days.feature.couple.impl

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 커플 관리 → 자동 기념일 목록 (기념일 숨김). 이 기능 안에서만 이동한다 */
@Serializable
internal data object AnniversaryListKey : NavKey
