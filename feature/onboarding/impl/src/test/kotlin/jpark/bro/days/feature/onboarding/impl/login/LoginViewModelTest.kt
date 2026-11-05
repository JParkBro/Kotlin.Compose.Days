package jpark.bro.days.feature.onboarding.impl.login

import app.cash.turbine.test
import jpark.bro.days.core.model.AuthProvider
import jpark.bro.days.core.testing.FakeAuthRepository
import jpark.bro.days.core.testing.FakeOnboardingRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val onboardingRepository = FakeOnboardingRepository()
    private val viewModel by lazy { LoginViewModel(authRepository, onboardingRepository) }

    @Test
    fun `구글 버튼을 누르면 계정 선택을 요청하고 버튼을 막음`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(LoginAction.GoogleLoginClicked)
                assertEquals(LoginEvent.RequestGoogleIdToken, awaitItem())
            }
            assertTrue(viewModel.state.value.isSigningIn)
        }

    @Test
    fun `로그인 중에 다시 눌러도 요청하지 않음`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(LoginAction.GoogleLoginClicked)
                viewModel.onAction(LoginAction.GoogleLoginClicked)
                assertEquals(LoginEvent.RequestGoogleIdToken, awaitItem())
                expectNoEvents()
            }
        }

    @Test
    fun `토큰을 받으면 Firebase에 로그인하고 약관 동의로 감`() =
        runTest {
            viewModel.onAction(LoginAction.GoogleLoginClicked)

            viewModel.event.test {
                skipItems(1)
                viewModel.onAction(LoginAction.GoogleIdTokenReceived("token-1"))
                assertEquals(LoginEvent.LoggedIn(termsAgreed = false), awaitItem())
            }
            assertEquals(listOf("token-1"), authRepository.googleIdTokens)
            assertNotNull(authRepository.currentUser())
            assertFalse(viewModel.state.value.isSigningIn)
        }

    @Test
    fun `이미 약관에 동의한 계정이면 온보딩을 끝냄`() =
        runTest {
            onboardingRepository.termsAgreed = true
            viewModel.onAction(LoginAction.GoogleLoginClicked)

            viewModel.event.test {
                skipItems(1)
                viewModel.onAction(LoginAction.GoogleIdTokenReceived("token-1"))
                assertEquals(LoginEvent.LoggedIn(termsAgreed = true), awaitItem())
            }
        }

    @Test
    fun `동의 기록을 확인하지 못하면 약관을 다시 받음`() =
        runTest {
            onboardingRepository.termsAgreed = true
            onboardingRepository.failNext = true
            viewModel.onAction(LoginAction.GoogleLoginClicked)

            viewModel.event.test {
                skipItems(1)
                viewModel.onAction(LoginAction.GoogleIdTokenReceived("token-1"))
                assertEquals(LoginEvent.LoggedIn(termsAgreed = false), awaitItem())
            }
        }

    @Test
    fun `Firebase 로그인에 실패하면 알리고 다시 누를 수 있음`() =
        runTest {
            authRepository.failNext = true
            viewModel.onAction(LoginAction.GoogleLoginClicked)

            viewModel.event.test {
                skipItems(1)
                viewModel.onAction(LoginAction.GoogleIdTokenReceived("token-1"))
                assertEquals(LoginEvent.LoginFailed, awaitItem())
            }
            assertFalse(viewModel.state.value.isSigningIn)
        }

    @Test
    fun `계정 선택 창을 닫으면 알리지 않고 다시 누를 수 있음`() =
        runTest {
            viewModel.onAction(LoginAction.GoogleLoginClicked)

            viewModel.event.test {
                skipItems(1)
                viewModel.onAction(LoginAction.GoogleIdTokenFailed(cancelled = true))
                expectNoEvents()
            }
            assertFalse(viewModel.state.value.isSigningIn)
        }

    @Test
    fun `계정 선택이 오류로 실패하면 알림`() =
        runTest {
            viewModel.onAction(LoginAction.GoogleLoginClicked)

            viewModel.event.test {
                skipItems(1)
                viewModel.onAction(LoginAction.GoogleIdTokenFailed(cancelled = false))
                assertEquals(LoginEvent.LoginFailed, awaitItem())
            }
        }

    @Test
    fun `카카오 버튼을 누르면 카카오 로그인을 요청하고 버튼을 막음`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(LoginAction.KakaoLoginClicked)
                assertEquals(LoginEvent.RequestKakaoAccessToken, awaitItem())
            }
            assertTrue(viewModel.state.value.isSigningIn)
        }

    @Test
    fun `구글 로그인 중에는 카카오 버튼을 눌러도 요청하지 않음`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(LoginAction.GoogleLoginClicked)
                viewModel.onAction(LoginAction.KakaoLoginClicked)
                assertEquals(LoginEvent.RequestGoogleIdToken, awaitItem())
                expectNoEvents()
            }
        }

    @Test
    fun `카카오 토큰을 받으면 Firebase에 로그인하고 다음으로 감`() =
        runTest {
            viewModel.onAction(LoginAction.KakaoLoginClicked)

            viewModel.event.test {
                skipItems(1)
                viewModel.onAction(LoginAction.KakaoAccessTokenReceived("kakao-token-1"))
                assertEquals(LoginEvent.LoggedIn(termsAgreed = false), awaitItem())
            }
            assertEquals(listOf("kakao-token-1"), authRepository.kakaoAccessTokens)
            assertEquals(AuthProvider.KAKAO, authRepository.currentUser()?.provider)
        }

    @Test
    fun `카카오 토큰 교환에 실패하면 알리고 다시 누를 수 있음`() =
        runTest {
            authRepository.failNext = true
            viewModel.onAction(LoginAction.KakaoLoginClicked)

            viewModel.event.test {
                skipItems(1)
                viewModel.onAction(LoginAction.KakaoAccessTokenReceived("kakao-token-1"))
                assertEquals(LoginEvent.LoginFailed, awaitItem())
            }
            assertFalse(viewModel.state.value.isSigningIn)
        }

    @Test
    fun `카카오 로그인 창을 닫으면 알리지 않고 다시 누를 수 있음`() =
        runTest {
            viewModel.onAction(LoginAction.KakaoLoginClicked)

            viewModel.event.test {
                skipItems(1)
                viewModel.onAction(LoginAction.KakaoAccessTokenFailed(cancelled = true))
                expectNoEvents()
            }
            assertFalse(viewModel.state.value.isSigningIn)
        }

    @Test
    fun `카카오 로그인이 오류로 실패하면 알림`() =
        runTest {
            viewModel.onAction(LoginAction.KakaoLoginClicked)

            viewModel.event.test {
                skipItems(1)
                viewModel.onAction(LoginAction.KakaoAccessTokenFailed(cancelled = false))
                assertEquals(LoginEvent.LoginFailed, awaitItem())
            }
            assertFalse(viewModel.state.value.isSigningIn)
        }
}
