package jpark.bro.days.feature.couple.impl.anniversaries

import app.cash.turbine.test
import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.Couple
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.UserProfile
import jpark.bro.days.core.testing.FakeAnniversaryRepository
import jpark.bro.days.core.testing.FakeCoupleRepository
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

/** product-spec 9장: 현우(나) · 지민, 사귄 날 2025.06.14 (1일 세기), 오늘 2026.11.05 */
class AnniversaryListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val couples =
        FakeCoupleRepository(Couple("couple-1", "user-jimin", LocalDate.of(2025, 6, 14), countFirstDayAsOne = true))
    private val profiles =
        FakeProfileRepository(
            me = UserProfile("user-me", "현우", LocalDate.of(1998, 4, 12)),
            partner = UserProfile("user-jimin", "지민", LocalDate.of(1999, 9, 28)),
        )
    private val anniversaries = FakeAnniversaryRepository(initial = setOf("DAY_700"))
    private val viewModel by lazy { AnniversaryListViewModel(couples, profiles, anniversaries, TestClock) }

    @Test
    fun `앞으로 1년의 기념일을 날짜순으로 보여주고 숨긴 것을 표시`() {
        val rows = viewModel.state.value.rows

        assertEquals(
            listOf("DAY_600", "BDAY_user-me_2027", "DAY_700", "YEAR_2", "DAY_800", "BDAY_user-jimin_2027"),
            rows.map { it.anniversary.ruleKey },
        )
        assertEquals(DdayCount.Remaining(90), rows.first().dday)
        assertTrue(rows.first { it.anniversary == AutoAnniversary.Days(700, LocalDate.of(2027, 5, 14)) }.isHidden)
        assertFalse(rows.first().isHidden)
        assertEquals("지민", viewModel.state.value.nicknames["user-jimin"])
    }

    @Test
    fun `끄면 숨기고 켜면 다시 보임`() {
        viewModel.onAction(AnniversaryListAction.VisibilityChanged("DAY_600", visible = false))
        assertTrue(
            viewModel.state.value.rows
                .first()
                .isHidden,
        )

        viewModel.onAction(AnniversaryListAction.VisibilityChanged("DAY_700", visible = true))
        assertEquals(setOf("DAY_600"), anniversaries.hidden.value)
    }

    @Test
    fun `저장에 실패하면 알림`() =
        runTest {
            anniversaries.failure = IllegalStateException("db")
            viewModel.event.test {
                viewModel.onAction(AnniversaryListAction.VisibilityChanged("DAY_600", visible = false))
                assertEquals(AnniversaryListEvent.SaveFailed, awaitItem())
            }
        }

    @Test
    fun `보는 중에 연결이 끊기면 뒤로 감`() =
        runTest {
            viewModel.event.test {
                couples.couple.value = null
                assertEquals(AnniversaryListEvent.NavigateBack, awaitItem())
            }
        }
}
