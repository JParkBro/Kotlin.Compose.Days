package jpark.bro.days.core.data.repository

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.session.UserSession
import jpark.bro.days.core.datastore.SettingsDataStore
import jpark.bro.days.core.firebase.sync.MarketingConsentDto
import jpark.bro.days.core.firebase.sync.TermsAgreementDto
import jpark.bro.days.core.firebase.sync.TermsRemoteDataSource
import jpark.bro.days.core.model.MarketingConsent
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Clock

internal class DefaultOnboardingRepository(
    private val settingsDataStore: SettingsDataStore,
    private val userSession: UserSession,
    private val remote: TermsRemoteDataSource,
    private val clock: Clock,
) : OnboardingRepository {
    override suspend fun isIntroSeen(): Boolean = settingsDataStore.introSeen()

    override suspend fun markIntroSeen() {
        settingsDataStore.setIntroSeen()
    }

    override suspend fun isOnboarded(): Boolean {
        val userId = userSession.currentUserIdOrNull() ?: return false
        return settingsDataStore.termsAgreed(userId)
    }

    override suspend fun checkTermsAgreed(): Result<Boolean> =
        suspendRunCatching {
            val userId = userSession.currentUserId()
            settingsDataStore.termsAgreed(userId) ||
                (remoteCall { remote.agreedTermsVersion(userId) } != null).also { agreed ->
                    if (agreed) settingsDataStore.setTermsAgreed(userId)
                }
        }

    override suspend fun agreeTerms(marketing: Boolean): Result<Unit> =
        suspendRunCatching {
            val userId = userSession.currentUserId()
            val agreement =
                TermsAgreementDto(
                    termsVersion = TERMS_VERSION,
                    privacyVersion = PRIVACY_VERSION,
                    ageOver14 = true,
                    marketing = marketing,
                    agreedAt = clock.millis(),
                )
            remoteCall { remote.pushAgreement(userId, agreement) }
            settingsDataStore.setTermsAgreed(userId)
        }

    override suspend fun marketingConsent(): Result<MarketingConsent> =
        suspendRunCatching {
            val userId = userSession.currentUserId()
            val consent = remoteCall { remote.marketingConsent(userId) }
            MarketingConsent(agreed = consent?.agreed ?: false, updatedAt = consent?.updatedAt)
        }

    override suspend fun setMarketingConsent(agreed: Boolean): Result<MarketingConsent> =
        suspendRunCatching {
            val userId = userSession.currentUserId()
            val consent = MarketingConsentDto(agreed = agreed, updatedAt = clock.millis())
            remoteCall { remote.pushMarketingConsent(userId, consent) }
            MarketingConsent(agreed = consent.agreed, updatedAt = consent.updatedAt)
        }

    /**
     * 오프라인이면 Firestore 쓰기가 연결될 때까지 끝나지 않아 시간 제한을 둔다.
     * withTimeout의 예외는 취소로 취급돼 suspendRunCatching이 잡지 않으므로 null로 받아 실패로 바꾼다
     */
    private suspend fun <T> remoteCall(block: suspend () -> T): T {
        val result = withTimeoutOrNull(REMOTE_TIMEOUT_MILLIS) { Wrapped(block()) }
        return checkNotNull(result) { "서버 응답이 없습니다." }.value
    }

    private class Wrapped<T>(
        val value: T,
    )

    companion object {
        /** 정책 문서(policy-site, 9단계)의 시행일. 문서를 바꾸면 함께 올린다 */
        const val TERMS_VERSION = "2026-11-05"
        const val PRIVACY_VERSION = "2026-11-05"
        private const val REMOTE_TIMEOUT_MILLIS = 10_000L
    }
}
