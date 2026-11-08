package jpark.bro.days.feature.settings.impl.withdraw

import app.cash.turbine.test
import jpark.bro.days.core.data.account.AccountMismatchException
import jpark.bro.days.core.model.AuthProvider
import jpark.bro.days.core.model.AuthUser
import jpark.bro.days.core.model.Couple
import jpark.bro.days.core.model.Reauthentication
import jpark.bro.days.core.model.UserProfile
import jpark.bro.days.core.testing.FakeAccountRepository
import jpark.bro.days.core.testing.FakeAuthRepository
import jpark.bro.days.core.testing.FakeCoupleRepository
import jpark.bro.days.core.testing.FakeProfileRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class WithdrawViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val account = FakeAccountRepository()
    private val auth = FakeAuthRepository(AuthUser("kakao:42", AuthProvider.KAKAO, email = null))
    private val couple = FakeCoupleRepository(Couple("couple-1", "user-jimin", LocalDate.of(2025, 6, 14), true))
    private val profiles = FakeProfileRepository(partner = UserProfile("user-jimin", "지민", null))

    private fun viewModel() = WithdrawViewModel(account, auth, couple, profiles)

    @Test
    fun `로그인 수단과 커플 상대를 보여주고 확인하기 전에는 탈퇴할 수 없음`() {
        val viewModel = viewModel()
        val state = viewModel.state.value
        assertEquals(AuthProvider.KAKAO, state.provider)
        assertTrue(state.isCoupled)
        assertEquals("지민", state.partnerName)
        assertFalse(state.canWithdraw)

        viewModel.onAction(WithdrawAction.ConfirmChanged(true))
        assertTrue(viewModel.state.value.canWithdraw)
    }

    @Test
    fun `탈퇴하기를 누르면 같은 수단으로 다시 로그인하고 확인되면 탈퇴`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onAction(WithdrawAction.ConfirmChanged(true))
            viewModel.event.test {
                viewModel.onAction(WithdrawAction.WithdrawClicked)
                assertEquals(WithdrawEvent.RequestReauthentication(AuthProvider.KAKAO), awaitItem())
                assertTrue(viewModel.state.value.isWorking)

                viewModel.onAction(WithdrawAction.Reauthenticated(Reauthentication.Kakao("token")))
                assertEquals(WithdrawEvent.Withdrawn, awaitItem())
            }
            assertEquals(listOf(Reauthentication.Kakao("token")), account.withdrawn)
        }

    @Test
    fun `확인하지 않으면 탈퇴하기를 눌러도 아무 일도 없음`() =
        runTest {
            val viewModel = viewModel()
            viewModel.event.test {
                viewModel.onAction(WithdrawAction.WithdrawClicked)
                expectNoEvents()
            }
        }

    @Test
    fun `다른 계정으로 확인하면 알리고 다시 시도할 수 있음`() =
        runTest {
            account.result = Result.failure(AccountMismatchException())
            val viewModel = viewModel()
            viewModel.onAction(WithdrawAction.ConfirmChanged(true))
            viewModel.event.test {
                viewModel.onAction(WithdrawAction.WithdrawClicked)
                awaitItem()
                viewModel.onAction(WithdrawAction.Reauthenticated(Reauthentication.Kakao("token")))
                assertEquals(WithdrawEvent.AccountMismatch, awaitItem())
            }
            assertFalse(viewModel.state.value.isWorking)
            assertTrue(viewModel.state.value.canWithdraw)
        }

    @Test
    fun `다시 로그인을 닫으면 조용히, 실패하면 알림`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onAction(WithdrawAction.ConfirmChanged(true))
            viewModel.event.test {
                viewModel.onAction(WithdrawAction.WithdrawClicked)
                awaitItem()
                viewModel.onAction(WithdrawAction.ReauthenticationFailed(cancelled = true))
                expectNoEvents()

                viewModel.onAction(WithdrawAction.WithdrawClicked)
                awaitItem()
                viewModel.onAction(WithdrawAction.ReauthenticationFailed(cancelled = false))
                assertEquals(WithdrawEvent.WithdrawFailed, awaitItem())
            }
            assertTrue(account.withdrawn.isEmpty())
        }
}
