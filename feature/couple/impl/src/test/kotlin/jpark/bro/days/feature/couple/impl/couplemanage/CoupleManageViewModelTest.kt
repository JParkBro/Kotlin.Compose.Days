package jpark.bro.days.feature.couple.impl.couplemanage

import app.cash.turbine.test
import jpark.bro.days.core.model.Couple
import jpark.bro.days.core.model.UserProfile
import jpark.bro.days.core.testing.FakeCoupleRepository
import jpark.bro.days.core.testing.FakeImageRepository
import jpark.bro.days.core.testing.FakeProfileRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import jpark.bro.days.feature.couple.impl.TestClock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class CoupleManageViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val couple = Couple("couple-1", "user-partner", LocalDate.of(2025, 6, 14), countFirstDayAsOne = true)
    private val repository = FakeCoupleRepository(couple)
    private val profiles = FakeProfileRepository(partner = UserProfile("user-partner", "지민", LocalDate.of(1999, 9, 28)))
    private val images = FakeImageRepository()
    private val viewModel by lazy { CoupleManageViewModel(repository, profiles, images, TestClock) }

    @Test
    fun `상대 이름 · 함께한 날을 보여줌`() {
        val state = viewModel.state.value
        assertEquals("지민", state.partner?.nickname)
        assertEquals(510, state.togetherDays)
    }

    @Test
    fun `사귄 날 · 1일로 세기를 바꾸면 저장`() {
        viewModel.onAction(PairCodeDate)
        assertEquals(LocalDate.of(2025, 6, 15), repository.couple.value?.startDate)

        viewModel.onAction(CoupleManageAction.CountFirstDayChanged(false))
        assertFalse(repository.couple.value!!.countFirstDayAsOne)
        assertEquals(508, viewModel.state.value.togetherDays)
    }

    @Test
    fun `저장에 실패하면 알림`() =
        runTest {
            repository.updateFailure = IllegalStateException("network")
            viewModel.event.test {
                viewModel.onAction(CoupleManageAction.CountFirstDayChanged(false))
                assertEquals(CoupleManageEvent.UpdateFailed, awaitItem())
            }
        }

    @Test
    fun `해제 시트에서 사본 삭제를 골라 해제하면 연결이 끝나고 이전 화면으로`() =
        runTest {
            viewModel.onAction(CoupleManageAction.UnpairClicked)
            assertTrue(viewModel.state.value.isUnpairSheetOpen)
            viewModel.onAction(CoupleManageAction.DeleteCopiesChanged(true))

            viewModel.event.test {
                viewModel.onAction(CoupleManageAction.UnpairConfirmed)
                assertEquals(CoupleManageEvent.Disconnected, awaitItem())
            }
            assertEquals(true, repository.lastDeleteCopies)
        }

    @Test
    fun `상대가 해제해도 이전 화면으로`() =
        runTest {
            viewModel.event.test {
                repository.couple.value = null
                assertEquals(CoupleManageEvent.Disconnected, awaitItem())
            }
        }

    private companion object {
        val PairCodeDate = CoupleManageAction.StartDateSelected(LocalDate.of(2025, 6, 15))
    }

    @Test
    fun `자동 기념일을 누르면 기념일 목록으로 감`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(CoupleManageAction.AnniversariesClicked)
                assertEquals(CoupleManageEvent.OpenAnniversaries, awaitItem())
            }
        }

    @Test
    fun `상대 프로필 사진이 있으면 보여줌`() {
        val face = images.add("partner-face")
        profiles.partnerProfile.value = profiles.partnerProfile.value?.copy(profileImageId = "partner-face")

        assertEquals(face, viewModel.state.value.partnerPhoto)
    }
}
