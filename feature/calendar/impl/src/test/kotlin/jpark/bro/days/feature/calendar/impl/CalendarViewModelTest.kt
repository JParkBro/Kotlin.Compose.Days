package jpark.bro.days.feature.calendar.impl

import app.cash.turbine.test
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.testing.FakeItemRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class CalendarViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val seoul = ZoneId.of("Asia/Seoul")
    private val clock = Clock.fixed(Today.atStartOfDay(seoul).toInstant(), seoul)
    private val repository = FakeItemRepository(listOf(Toeic, Allowance, SeongsuDinner, Lunch))
    private val viewModel by lazy { CalendarViewModel(repository, clock) }
    private val nov6 = LocalDate.of(2026, 11, 6)

    @Test
    fun `이번 달을 오늘 기준으로 보여주고 리스트는 이번 달 전체`() {
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(YearMonth.of(2026, 11), state.month)
        assertNull(state.selectedDate)
        assertEquals(4, state.calendar.entries.size)
        assertFalse(state.showTodayChip)
    }

    @Test
    fun `날짜를 고르면 그날 항목만 보이고 다시 누르면 선택이 풀림`() {
        viewModel.onAction(CalendarAction.DateClicked(nov6))
        assertEquals(nov6, viewModel.state.value.selectedDate)
        assertEquals(
            listOf(Lunch, SeongsuDinner),
            viewModel.state.value.calendar.entries
                .map { it.item },
        )

        viewModel.onAction(CalendarAction.DateClicked(nov6))
        assertNull(viewModel.state.value.selectedDate)
    }

    @Test
    fun `달을 옮기면 선택이 풀리고 오늘 칩을 누르면 이번 달 오늘로 돌아옴`() {
        viewModel.onAction(CalendarAction.DateClicked(nov6))

        viewModel.onAction(CalendarAction.NextMonthClicked)
        assertEquals(YearMonth.of(2026, 12), viewModel.state.value.month)
        assertNull(viewModel.state.value.selectedDate)
        assertTrue(viewModel.state.value.showTodayChip)
        // 매월 17일 반복은 12월에도 있음
        assertEquals(
            listOf(Allowance),
            viewModel.state.value.calendar.entries
                .map { it.item },
        )

        viewModel.onAction(CalendarAction.TodayClicked)
        assertEquals(YearMonth.of(2026, 11), viewModel.state.value.month)
        assertEquals(Today, viewModel.state.value.selectedDate)
    }

    @Test
    fun `일정 완료를 체크하면 저장되고 다시 누르면 해제됨`() =
        runTest {
            viewModel.onAction(CalendarAction.DoneToggled(SeongsuDinner.id))
            assertTrue(
                repository
                    .observeItem(SeongsuDinner.id)
                    .first()!!
                    .content.isDone,
            )

            viewModel.onAction(CalendarAction.DoneToggled(SeongsuDinner.id))
            assertFalse(
                repository
                    .observeItem(SeongsuDinner.id)
                    .first()!!
                    .content.isDone,
            )
        }

    @Test
    fun `완료 저장에 실패하면 알림`() =
        runTest {
            viewModel.state.value
            repository.failNext = true

            viewModel.event.test {
                viewModel.onAction(CalendarAction.DoneToggled(SeongsuDinner.id))
                assertEquals(CalendarEvent.DoneToggleFailed, awaitItem())
            }
        }

    @Test
    fun `디데이는 상세 일정은 수정으로 감`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(CalendarAction.ItemClicked(Toeic.id, ItemType.DDAY))
                assertEquals(CalendarEvent.OpenItemDetail(Toeic.id), awaitItem())

                viewModel.onAction(CalendarAction.ItemClicked(Lunch.id, ItemType.PLAN))
                assertEquals(CalendarEvent.OpenItemEdit(ItemType.PLAN, itemId = Lunch.id), awaitItem())
            }
        }

    @Test
    fun `날짜를 고른 채 추가하면 그 날짜를 채워서 엶`() =
        runTest {
            viewModel.onAction(CalendarAction.DateClicked(nov6))
            viewModel.onAction(CalendarAction.AddButtonClicked)
            assertTrue(viewModel.state.value.isAddSheetOpen)

            viewModel.event.test {
                viewModel.onAction(CalendarAction.AddClicked(ItemType.PLAN))
                assertEquals(CalendarEvent.OpenItemEdit(ItemType.PLAN, date = nov6), awaitItem())
            }
            assertFalse(viewModel.state.value.isAddSheetOpen)
        }

    @Test
    fun `날짜를 고르지 않고 추가하면 날짜 없이 엶`() =
        runTest {
            viewModel.onAction(CalendarAction.AddButtonClicked)

            viewModel.event.test {
                viewModel.onAction(CalendarAction.AddClicked(ItemType.DDAY))
                assertEquals(CalendarEvent.OpenItemEdit(ItemType.DDAY), awaitItem())
            }
        }
}
