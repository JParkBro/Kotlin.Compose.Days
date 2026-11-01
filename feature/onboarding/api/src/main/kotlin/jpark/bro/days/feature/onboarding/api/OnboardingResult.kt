package jpark.bro.days.feature.onboarding.api

sealed interface OnboardingResult {
    /** 혼자 시작 → 홈(솔로) */
    data object Solo : OnboardingResult

    /** 커플 코드로 연결 → 커플 연결 흐름 */
    data object Couple : OnboardingResult
}

/** 구현은 app에 둔다. */
fun interface OnboardingResultHandler {
    fun handle(result: OnboardingResult)
}
