package jpark.bro.days.feature.couple.impl.paircodeconfirm

import app.cash.turbine.test
import jpark.bro.days.core.data.couple.PairingException
import jpark.bro.days.core.model.PairCodeCheck
import jpark.bro.days.core.testing.FakeCoupleRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import jpark.bro.days.feature.couple.impl.PairCodeConfirmKey
import jpark.bro.days.feature.couple.impl.R
import jpark.bro.days.feature.couple.impl.TestClock
import jpark.bro.days.feature.couple.impl.Today
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class PairCodeConfirmViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCoupleRepository()
    private val viewModel by lazy { PairCodeConfirmViewModel(PairCodeConfirmKey("K7Q2MX"), repository, TestClock) }

    @Test
    fun `발급자 이름을 보여주고 사귄 날은 오늘 · 1일로 세기는 켜짐이 기본`() {
        val state = viewModel.state.value
        assertEquals("지민", state.partnerName)
        assertEquals(Today, state.startDate)
        assertTrue(state.countFirstDayAsOne)
        assertEquals(1, state.togetherDays)
        assertTrue(state.canConnect)
    }

    @Test
    fun `사귄 날을 바꾸면 함께한 날 미리보기가 바뀜 (product-spec 9장 510일)`() {
        viewModel.onAction(PairCodeConfirmAction.StartDateSelected(LocalDate.of(2025, 6, 14)))
        assertEquals(510, viewModel.state.value.togetherDays)

        viewModel.onAction(PairCodeConfirmAction.CountFirstDayChanged(false))
        assertEquals(509, viewModel.state.value.togetherDays)
    }

    @Test
    fun `그사이 만료된 코드면 연결할 수 없음`() {
        repository.checkResult = Result.success(PairCodeCheck.Expired)

        assertEquals(R.string.couple_code_expired, viewModel.state.value.codeError)
        assertFalse(viewModel.state.value.canConnect)
    }

    @Test
    fun `연결하면 홈으로`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(PairCodeConfirmAction.ConnectClicked)
                assertEquals(PairCodeConfirmEvent.Connected, awaitItem())
            }
            assertEquals(Today, repository.couple.value?.startDate)
        }

    @Test
    fun `연결 순간 코드를 쓸 수 없게 됐으면 이유를, 네트워크 실패면 알림을`() =
        runTest {
            repository.connectFailure = PairingException(PairCodeCheck.AlreadyUsed)
            viewModel.onAction(PairCodeConfirmAction.ConnectClicked)
            assertEquals(R.string.couple_code_used, viewModel.state.value.codeError)

            val other = PairCodeConfirmViewModel(PairCodeConfirmKey("K7Q2MX"), repository, TestClock)
            repository.connectFailure = IllegalStateException("network")
            other.event.test {
                other.onAction(PairCodeConfirmAction.ConnectClicked)
                assertEquals(PairCodeConfirmEvent.ConnectFailed, awaitItem())
            }
            assertTrue(other.state.value.canConnect)
        }
}
