package jpark.bro.days.core.data.repository

import jpark.bro.days.core.data.ME
import jpark.bro.days.core.data.PARTNER
import jpark.bro.days.core.data.Today
import jpark.bro.days.core.firebase.sync.TermsAgreementDto
import jpark.bro.days.core.model.MarketingConsent
import jpark.bro.days.core.testing.FakeTermsRemoteDataSource
import jpark.bro.days.core.testing.testSettingsDataStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultOnboardingRepositoryTest {
    private val session = SwitchableUserSession(ME)
    private val remote = FakeTermsRemoteDataSource()
    private val clock = MutableClock(Today)
    private val repository = DefaultOnboardingRepository(testSettingsDataStore(), session, remote, clock)

    @Test
    fun `소개는 한 번 보면 기기에 남음`() =
        runTest {
            assertFalse(repository.isIntroSeen())

            repository.markIntroSeen()

            assertTrue(repository.isIntroSeen())
        }

    @Test
    fun `약관에 동의하면 서버에 버전 · 마케팅을 남기고 온보딩 완료`() =
        runTest {
            assertFalse(repository.isOnboarded())

            assertTrue(repository.agreeTerms(marketing = true).isSuccess)

            val agreement = remote.agreements.getValue(ME)
            assertEquals(DefaultOnboardingRepository.TERMS_VERSION, agreement.termsVersion)
            assertTrue(agreement.ageOver14)
            assertTrue(agreement.marketing)
            assertEquals(Today.toEpochMilli(), agreement.agreedAt)
            assertTrue(repository.isOnboarded())
        }

    @Test
    fun `오프라인이면 동의가 실패하고 온보딩은 끝나지 않음`() =
        runTest {
            remote.offline = true

            assertTrue(repository.agreeTerms(marketing = false).isFailure)
            assertFalse(repository.isOnboarded())
        }

    @Test
    fun `다른 기기에서 동의한 계정은 서버 기록으로 확인하고 기기에 남김`() =
        runTest {
            remote.agreements[ME] = TermsAgreementDto("2026-11-05", "2026-11-05", true, false, 1)

            assertEquals(true, repository.checkTermsAgreed().getOrNull())

            remote.offline = true
            assertEquals(true, repository.checkTermsAgreed().getOrNull())
            assertTrue(repository.isOnboarded())
        }

    @Test
    fun `동의 기록은 계정별이고 로그아웃이면 온보딩 전`() =
        runTest {
            repository.agreeTerms(marketing = false)

            session.userId.value = PARTNER
            assertEquals(false, repository.checkTermsAgreed().getOrNull())
            assertFalse(repository.isOnboarded())

            session.userId.value = null
            assertFalse(repository.isOnboarded())
        }

    @Test
    fun `알림 설정에서 바꾼 적이 없으면 약관 동의 때 값과 시각`() =
        runTest {
            repository.agreeTerms(marketing = true).getOrThrow()

            assertEquals(
                MarketingConsent(agreed = true, updatedAt = Today.toEpochMilli()),
                repository.marketingConsent().getOrThrow(),
            )
        }

    @Test
    fun `동의 기록이 없으면 미동의`() =
        runTest {
            assertEquals(MarketingConsent(agreed = false, updatedAt = null), repository.marketingConsent().getOrThrow())
        }

    @Test
    fun `마케팅 동의를 철회하면 바꾼 시각과 함께 서버에 남음`() =
        runTest {
            repository.agreeTerms(marketing = true).getOrThrow()
            clock.now = Today.plusSeconds(60)

            val saved = repository.setMarketingConsent(agreed = false).getOrThrow()

            val expected = MarketingConsent(agreed = false, updatedAt = clock.now.toEpochMilli())
            assertEquals(expected, saved)
            assertEquals(expected, repository.marketingConsent().getOrThrow())
        }

    @Test
    fun `오프라인이면 마케팅 동의 조회 · 변경이 실패함`() =
        runTest {
            remote.offline = true

            assertTrue(repository.marketingConsent().isFailure)
            assertTrue(repository.setMarketingConsent(agreed = true).isFailure)
        }
}
