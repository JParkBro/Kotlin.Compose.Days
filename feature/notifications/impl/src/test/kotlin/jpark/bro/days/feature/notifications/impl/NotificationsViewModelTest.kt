package jpark.bro.days.feature.notifications.impl

import app.cash.turbine.test
import jpark.bro.days.core.model.ActivityContent
import jpark.bro.days.core.model.ActivityLog
import jpark.bro.days.core.model.ActivityType
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.UserProfile
import jpark.bro.days.core.testing.FakeActivityRepository
import jpark.bro.days.core.testing.FakeItemRepository
import jpark.bro.days.core.testing.FakeProfileRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** product-spec 9장 시나리오: 오늘 = 2026.11.05 */
class NotificationsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val seoul = ZoneId.of("Asia/Seoul")
    private val now = LocalDateTime.of(2026, 11, 5, 15, 0).atZone(seoul)
    private val clock = Clock.fixed(now.toInstant(), seoul)

    private fun millis(at: LocalDateTime) = at.atZone(seoul).toInstant().toEpochMilli()

    private val toeic =
        Item(
            id = "toeic",
            content =
                ItemContent(
                    type = ItemType.DDAY,
                    title = "토익 시험",
                    colorKey = "PINK",
                    iconKey = "SCHOOL",
                    date = LocalDate.of(2026, 11, 8),
                    countMode = CountMode.DDAY,
                ),
            ownerUserId = "user-me",
            updatedBy = "user-me",
            createdAt = 0,
            updatedAt = 0,
        )

    private val partnerAdded =
        ActivityLog(
            id = "a1",
            content =
                ActivityContent(
                    ActivityType.ITEM_ADDED,
                    "dinner",
                    ItemType.PLAN,
                    "성수 저녁",
                    actorUserId = "user-partner",
                ),
            createdAt = millis(now.toLocalDateTime().minusSeconds(20)),
            isRead = false,
        )
    private val reminder =
        ActivityLog(
            id = "a2",
            content = ActivityContent(ActivityType.REMINDER, "toeic", ItemType.DDAY, "토익 시험", offsetDays = 3),
            createdAt = millis(LocalDateTime.of(2026, 11, 4, 9, 0)),
            isRead = false,
        )
    private val anniversary =
        ActivityLog(
            id = "a3",
            content = ActivityContent(ActivityType.ANNIVERSARY, ruleKey = "BDAY_user-partner_2026"),
            createdAt = millis(LocalDateTime.of(2026, 10, 20, 9, 0)),
            isRead = true,
        )

    private val activities = FakeActivityRepository(listOf(partnerAdded, reminder, anniversary))
    private val viewModel by lazy {
        NotificationsViewModel(
            activityRepository = activities,
            itemRepository = FakeItemRepository(listOf(toeic)),
            profileRepository =
                FakeProfileRepository(
                    me = UserProfile("user-me", "현우", null),
                    partner = UserProfile("user-partner", "지민", null),
                ),
            clock = clock,
        )
    }

    @Test
    fun `오늘 이번 주 이전으로 나누고 지난 시간과 이름을 채움`() {
        val state = viewModel.state.value

        assertEquals(
            listOf(ActivityGroup.TODAY, ActivityGroup.THIS_WEEK, ActivityGroup.EARLIER),
            state.sections.map {
                it.group
            },
        )
        assertEquals(2, state.unreadCount)
        val (today, week, earlier) = state.sections.map { it.rows.single() }
        assertEquals(Elapsed.JustNow, today.elapsed)
        assertEquals("지민", today.actorName)
        assertEquals(Elapsed.Yesterday, week.elapsed)
        assertEquals(Elapsed.Days(16), earlier.elapsed)
        assertEquals(AnniversaryLabel.Birthday("지민"), earlier.anniversary)
    }

    @Test
    fun `남아 있는 항목만 열 수 있음`() {
        val rows =
            viewModel.state.value.sections
                .flatMap { it.rows }
                .associateBy { it.id }

        // 상대가 추가한 일정이 아직 이 기기에 없으면(또는 지워졌으면) 열지 않는다
        assertNull(rows.getValue("a1").target)
        assertEquals(OpenTarget("toeic", ItemType.DDAY), rows.getValue("a2").target)
        assertNull(rows.getValue("a3").target)
    }

    @Test
    fun `누르면 읽음 처리하고 항목을 엶`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(NotificationsAction.RowClicked("a2"))
                assertEquals(NotificationsEvent.OpenItem("toeic", ItemType.DDAY), awaitItem())
            }
            assertTrue(
                activities
                    .observeActivities()
                    .first()
                    .single { it.id == "a2" }
                    .isRead,
            )
            assertEquals(1, viewModel.state.value.unreadCount)
        }

    @Test
    fun `모두 읽음`() =
        runTest {
            viewModel.onAction(NotificationsAction.MarkAllReadClicked)

            assertEquals(0, viewModel.state.value.unreadCount)
        }

    @Test
    fun `모두 읽음에 실패하면 알림`() =
        runTest {
            viewModel.event.test {
                activities.failNext = true
                viewModel.onAction(NotificationsAction.MarkAllReadClicked)
                assertEquals(NotificationsEvent.MarkReadFailed, awaitItem())
            }
        }

    @Test
    fun `알림 설정 바로가기`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(NotificationsAction.SettingsClicked)
                assertEquals(NotificationsEvent.OpenSettings, awaitItem())
            }
        }
}
