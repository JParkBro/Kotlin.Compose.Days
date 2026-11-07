package jpark.bro.days.core.testing

import jpark.bro.days.core.firebase.sync.MarketingConsentDto
import jpark.bro.days.core.firebase.sync.TermsAgreementDto
import jpark.bro.days.core.firebase.sync.TermsRemoteDataSource

class FakeTermsRemoteDataSource : TermsRemoteDataSource {
    /** 계정별로 서버에 남은 동의 기록 */
    val agreements = mutableMapOf<String, TermsAgreementDto>()

    /** true면 서버 호출이 실패한다 (오프라인) */
    var offline: Boolean = false

    override suspend fun pushAgreement(
        uid: String,
        agreement: TermsAgreementDto,
    ) {
        check(!offline) { "오프라인 (테스트)" }
        agreements[uid] = agreement
    }

    override suspend fun agreedTermsVersion(uid: String): String? {
        check(!offline) { "오프라인 (테스트)" }
        return agreements[uid]?.termsVersion
    }

    /** 알림 설정에서 바꾼 마케팅 동의. 없으면 약관 동의 기록의 값 */
    val marketingConsents = mutableMapOf<String, MarketingConsentDto>()

    override suspend fun marketingConsent(uid: String): MarketingConsentDto? {
        check(!offline) { "오프라인 (테스트)" }
        return marketingConsents[uid] ?: agreements[uid]?.let { MarketingConsentDto(it.marketing, it.agreedAt) }
    }

    override suspend fun pushMarketingConsent(
        uid: String,
        consent: MarketingConsentDto,
    ) {
        check(!offline) { "오프라인 (테스트)" }
        marketingConsents[uid] = consent
    }
}
