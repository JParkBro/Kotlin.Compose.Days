package jpark.bro.days.feature.onboarding.impl.login

import app.cash.turbine.test
import jpark.bro.days.core.testing.FakeAuthRepository
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
    private val viewModel by lazy { LoginViewModel(authRepository) }

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
    fun `토큰을 받으면 Firebase에 로그인하고 다음으로 감`() =
        runTest {
            viewModel.onAction(LoginAction.GoogleLoginClicked)

            viewModel.event.test {
                skipItems(1)
                viewModel.onAction(LoginAction.GoogleIdTokenReceived("token-1"))
                assertEquals(LoginEvent.LoggedIn, awaitItem())
            }
            assertEquals(listOf("token-1"), authRepository.googleIdTokens)
            assertNotNull(authRepository.currentUser())
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
}
