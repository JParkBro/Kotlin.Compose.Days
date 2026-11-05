package jpark.bro.days.core.data.repository

import jpark.bro.days.core.data.ME
import jpark.bro.days.core.data.PARTNER
import jpark.bro.days.core.data.Today
import jpark.bro.days.core.firebase.sync.TermsAgreementDto
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
    private val repository =
        DefaultOnboardingRepository(testSettingsDataStore(), session, remote, MutableClock(Today))

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
}
