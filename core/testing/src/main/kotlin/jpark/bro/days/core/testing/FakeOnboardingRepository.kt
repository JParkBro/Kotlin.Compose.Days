package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.OnboardingRepository
import jpark.bro.days.core.model.MarketingConsent

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

    /** 서버에 있는 마케팅 수신 동의 */
    var marketingConsent: MarketingConsent = MarketingConsent(agreed = false, updatedAt = null)

    /** setMarketingConsent가 남기는 시각 */
    var now: Long = 0L

    override suspend fun marketingConsent(): Result<MarketingConsent> =
        if (consumeFailure()) Result.failure(IllegalStateException("조회 실패 (테스트)")) else Result.success(marketingConsent)

    override suspend fun setMarketingConsent(agreed: Boolean): Result<MarketingConsent> {
        if (consumeFailure()) return Result.failure(IllegalStateException("저장 실패 (테스트)"))
        marketingConsent = MarketingConsent(agreed = agreed, updatedAt = now)
        return Result.success(marketingConsent)
    }

    private fun consumeFailure(): Boolean = failNext.also { failNext = false }
}
