package jpark.bro.days.core.testing

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
}
