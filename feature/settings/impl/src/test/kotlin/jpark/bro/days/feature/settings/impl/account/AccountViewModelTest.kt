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
import org.junit.Assert.assertTrue
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
    fun `로그아웃을 누르면 데이터 삭제 확인 창을 띄우고 취소하면 닫음`() {
        viewModel.onAction(AccountAction.LogoutClicked)
        assertTrue(viewModel.state.value.isLogoutConfirmVisible)
        assertEquals(user, authRepository.currentUser())

        viewModel.onAction(AccountAction.LogoutDismissed)
        assertFalse(viewModel.state.value.isLogoutConfirmVisible)
        assertEquals(user, authRepository.currentUser())
    }

    @Test
    fun `확인 창에서 로그아웃하면 세션을 끝냄`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(AccountAction.LogoutClicked)
                viewModel.onAction(AccountAction.LogoutConfirmed)
                assertEquals(AccountEvent.LoggedOut, awaitItem())
            }
            assertNull(authRepository.currentUser())
            assertFalse(viewModel.state.value.isLogoutConfirmVisible)
        }

    @Test
    fun `로그아웃에 실패하면 알리고 다시 누를 수 있음`() =
        runTest {
            authRepository.failNext = true

            viewModel.event.test {
                viewModel.onAction(AccountAction.LogoutConfirmed)
                assertEquals(AccountEvent.LogoutFailed, awaitItem())
            }
            assertFalse(viewModel.state.value.isSigningOut)
        }
}
