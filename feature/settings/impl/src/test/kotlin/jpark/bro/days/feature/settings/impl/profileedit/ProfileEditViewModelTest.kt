package jpark.bro.days.feature.settings.impl.profileedit

import app.cash.turbine.test
import jpark.bro.days.core.model.UserProfile
import jpark.bro.days.core.testing.FakeProfileRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class ProfileEditViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** product-spec 9장: 오늘 = 2026.11.05 */
    private val seoul = ZoneId.of("Asia/Seoul")
    private val clock = Clock.fixed(LocalDate.of(2026, 11, 5).atStartOfDay(seoul).toInstant(), seoul)
    private val saved = UserProfile("user-me", "현우", LocalDate.of(1998, 4, 12))
    private val repository = FakeProfileRepository(me = saved)
    private val viewModel by lazy { ProfileEditViewModel(repository, clock) }

    @Test
    fun `저장된 프로필로 채우고 바뀐 것이 없으면 저장할 수 없음`() {
        val state = viewModel.state.value
        assertEquals("현우", state.nickname)
        assertEquals(LocalDate.of(1998, 4, 12), state.birthDate)
        assertEquals(LocalDate.of(2026, 11, 5), state.today)
        assertFalse(state.canSave)
    }

    @Test
    fun `이름은 10자까지만 받고 공백만 있으면 저장할 수 없음`() {
        viewModel.onAction(ProfileEditAction.NicknameChanged("가".repeat(12)))
        assertEquals(10, viewModel.state.value.nickname.length)
        assertTrue(viewModel.state.value.canSave)

        viewModel.onAction(ProfileEditAction.NicknameChanged("   "))
        assertFalse(viewModel.state.value.canSave)
    }

    @Test
    fun `생일을 고르거나 지우면 저장할 수 있음`() {
        viewModel.onAction(ProfileEditAction.BirthDateClicked)
        assertTrue(viewModel.state.value.isDatePickerOpen)

        viewModel.onAction(ProfileEditAction.BirthDateSelected(LocalDate.of(1998, 4, 13)))
        assertFalse(viewModel.state.value.isDatePickerOpen)
        assertTrue(viewModel.state.value.canSave)

        viewModel.onAction(ProfileEditAction.BirthDateCleared)
        assertNull(viewModel.state.value.birthDate)
        assertTrue(viewModel.state.value.canSave)
    }

    @Test
    fun `저장하면 프로필이 바뀌고 이전 화면으로`() =
        runTest {
            viewModel.onAction(ProfileEditAction.NicknameChanged(" 현우 🌱 "))

            viewModel.event.test {
                viewModel.onAction(ProfileEditAction.SaveClicked)
                assertEquals(ProfileEditEvent.Saved, awaitItem())
            }
            assertEquals("현우 🌱", repository.myProfile.value?.nickname)
        }

    @Test
    fun `저장에 실패하면 알리고 다시 누를 수 있음`() =
        runTest {
            repository.failNext = true
            viewModel.onAction(ProfileEditAction.NicknameChanged("현우2"))

            viewModel.event.test {
                viewModel.onAction(ProfileEditAction.SaveClicked)
                assertEquals(ProfileEditEvent.SaveFailed, awaitItem())
            }
            assertTrue(viewModel.state.value.canSave)
        }

    @Test
    fun `처음 설정할 때는 비어 있음`() {
        val empty = ProfileEditViewModel(FakeProfileRepository(), clock)

        assertEquals("", empty.state.value.nickname)
        assertFalse(empty.state.value.canSave)
    }
}
