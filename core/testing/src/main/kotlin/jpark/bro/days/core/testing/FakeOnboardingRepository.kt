package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.OnboardingRepository

class FakeOnboardingRepository(
    var introSeen: Boolean = false,
    /** 로그인 후 확인 결과: 이미 동의한 계정인지 */
    var termsAgreed: Boolean = false,
) : OnboardingRepository {
    /** agreeTerms로 받은 마케팅 수신 동의 */
    val marketingAgreements = mutableListOf<Boolean>()

    /** true면 다음 확인 · 동의가 실패한다 */
    var failNext: Boolean = false

    override suspend fun isIntroSeen(): Boolean = introSeen

    override suspend fun markIntroSeen() {
        introSeen = true
    }

    override suspend fun isOnboarded(): Boolean = termsAgreed

    override suspend fun checkTermsAgreed(): Result<Boolean> =
        if (consumeFailure()) Result.failure(IllegalStateException("확인 실패 (테스트)")) else Result.success(termsAgreed)

    override suspend fun agreeTerms(marketing: Boolean): Result<Unit> {
        if (consumeFailure()) return Result.failure(IllegalStateException("동의 저장 실패 (테스트)"))
        marketingAgreements += marketing
        termsAgreed = true
        return Result.success(Unit)
    }

    private fun consumeFailure(): Boolean = failNext.also { failNext = false }
}
