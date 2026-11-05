package jpark.bro.days.feature.settings.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 정책 문서 (Firebase Hosting 페이지를 웹뷰로 연다, 9단계에서 배포) */
enum class PolicyType {
    PRIVACY,
    TERMS,
}

/** 정책 문서 보기. 온보딩 약관 동의의 "보기"에서도 연다 */
@Serializable
data class PolicyDocKey(
    val type: PolicyType,
) : NavKey
