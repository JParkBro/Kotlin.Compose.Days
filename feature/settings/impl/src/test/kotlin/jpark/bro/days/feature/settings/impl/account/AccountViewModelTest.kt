package jpark.bro.days.feature.settings.impl.account

import app.cash.turbine.test
import jpark.bro.days.core.model.AuthProvider
import jpark.bro.days.core.model.AuthUser
import jpark.bro.days.core.testing.FakeAuthRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class AccountViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val user = AuthUser("google-1", AuthProvider.GOOGLE, email = "hyunwoo@example.com")
    private val authRepository = FakeAuthRepository(user)
    private val viewModel by lazy { AccountViewModel(authRepository) }

    @Test
    fun `로그인 수단과 이메일을 보여줌`() {
        assertEquals(user, viewModel.state.value.user)
    }

    @Test
    fun `로그아웃하면 세션을 끝냄`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(AccountAction.LogoutClicked)
                assertEquals(AccountEvent.LoggedOut, awaitItem())
            }
            assertNull(authRepository.currentUser())
        }

    @Test
    fun `로그아웃에 실패하면 알리고 다시 누를 수 있음`() =
        runTest {
            authRepository.failNext = true

            viewModel.event.test {
                viewModel.onAction(AccountAction.LogoutClicked)
                assertEquals(AccountEvent.LogoutFailed, awaitItem())
            }
            assertFalse(viewModel.state.value.isSigningOut)
        }
}
