package jpark.bro.days.feature.couple.impl.pairing

import app.cash.turbine.test
import jpark.bro.days.core.data.couple.ProfileRequiredException
import jpark.bro.days.core.model.Couple
import jpark.bro.days.core.model.PairCodeCheck
import jpark.bro.days.core.testing.FakeCoupleRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import jpark.bro.days.feature.couple.impl.R
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class PairingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCoupleRepository()
    private val viewModel by lazy { PairingViewModel(repository) }

    @Test
    fun `열면 내 코드를 발급해 보여줌`() {
        assertEquals(
            "K7Q2MX",
            viewModel.state.value.myCode
                ?.code,
        )
        assertFalse(viewModel.state.value.isIssuing)
    }

    @Test
    fun `이름이 없으면 프로필 설정을 안내하고 누르면 프로필 수정으로`() =
        runTest {
            repository.issueResult = Result.failure(ProfileRequiredException())

            assertEquals(IssueError.PROFILE_REQUIRED, viewModel.state.value.issueError)
            viewModel.event.test {
                viewModel.onAction(PairingAction.SetProfileClicked)
                assertEquals(PairingEvent.OpenProfileEdit, awaitItem())
            }
        }

    @Test
    fun `코드 복사를 누르면 내 코드를 복사함`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(PairingAction.CopyClicked)
                assertEquals(PairingEvent.CopyCode("K7Q2MX"), awaitItem())
            }
        }

    @Test
    fun `입력은 허용 문자만 대문자로 채우고 붙여넣기는 6칸까지`() {
        viewModel.onAction(PairingAction.CharsTyped("k7q-0o1i2mxz9"))

        assertEquals("K7Q2MX", viewModel.state.value.input)
        assertEquals(5, viewModel.state.value.activeCell)
        assertTrue(viewModel.state.value.canConnect)
    }

    @Test
    fun `칸을 누르고 입력하면 그 칸을 덮어쓰고 다음 칸으로`() {
        viewModel.onAction(PairingAction.CharsTyped("K7Q2MX"))

        viewModel.onAction(PairingAction.CellClicked(2))
        viewModel.onAction(PairingAction.CharsTyped("w"))

        assertEquals("K7W2MX", viewModel.state.value.input)
        assertEquals(3, viewModel.state.value.activeCell)
    }

    @Test
    fun `지우기는 선택한 칸을, 빈 칸이면 앞 칸을 지움`() {
        viewModel.onAction(PairingAction.CharsTyped("K7Q"))
        assertEquals(3, viewModel.state.value.activeCell)

        viewModel.onAction(PairingAction.DeletePressed)
        assertEquals(listOf('K', '7', null, null, null, null), viewModel.state.value.cells)
        assertEquals(2, viewModel.state.value.activeCell)

        viewModel.onAction(PairingAction.CellClicked(0))
        viewModel.onAction(PairingAction.DeletePressed)
        assertEquals(listOf(null, '7', null, null, null, null), viewModel.state.value.cells)
        assertNull(viewModel.state.value.input)
        assertFalse(viewModel.state.value.canConnect)
    }

    @Test
    fun `유효한 코드면 확인 화면으로, 아니면 이유를 보여줌`() =
        runTest {
            viewModel.onAction(PairingAction.CharsTyped("K7Q2MX"))
            viewModel.event.test {
                viewModel.onAction(PairingAction.ConnectClicked)
                assertEquals(PairingEvent.NavigateToConfirm("K7Q2MX"), awaitItem())
            }

            repository.checkResult = Result.success(PairCodeCheck.Expired)
            viewModel.onAction(PairingAction.ConnectClicked)
            assertEquals(R.string.couple_code_expired, viewModel.state.value.inputError)

            viewModel.onAction(PairingAction.DeletePressed)
            assertNull(viewModel.state.value.inputError)
            assertFalse(viewModel.state.value.canConnect)
        }

    @Test
    fun `상대가 내 코드로 연결하면 홈으로`() =
        runTest {
            viewModel.event.test {
                repository.couple.value = Couple("couple-1", "user-partner", LocalDate.of(2025, 6, 14), true)
                assertEquals(PairingEvent.Connected, awaitItem())
            }
        }
}
