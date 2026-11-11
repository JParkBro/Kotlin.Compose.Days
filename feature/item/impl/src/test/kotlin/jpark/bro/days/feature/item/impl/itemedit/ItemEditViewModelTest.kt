package jpark.bro.days.feature.item.impl.itemedit

import app.cash.turbine.test
import jpark.bro.days.core.ads.RewardedAdRequest
import jpark.bro.days.core.ads.RewardedAdResult
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.Category
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.Couple
import jpark.bro.days.core.model.ImageUploadQuota
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.ItemReminder
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import jpark.bro.days.core.model.Visibility
import jpark.bro.days.core.testing.FakeCoupleRepository
import jpark.bro.days.core.testing.FakeImageQuotaRepository
import jpark.bro.days.core.testing.FakeImageRepository
import jpark.bro.days.core.testing.FakeItemRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import jpark.bro.days.feature.item.api.ItemEditKey
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
import java.time.LocalTime
import java.time.ZoneId

/** product-spec 9장 시나리오 (오늘 = 2026.11.05) */
class ItemEditViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val seoul = ZoneId.of("Asia/Seoul")
    private val today = LocalDate.of(2026, 11, 5)
    private val clock = Clock.fixed(today.atStartOfDay(seoul).toInstant(), seoul)

    /** 솔로: 금연 (D+, 1일 세기) 2026.08.07부터 */
    private val quitSmoking =
        Item(
            id = "quit",
            content =
                ItemContent(
                    type = ItemType.DDAY,
                    title = "금연",
                    colorKey = AppThemeType.SAGE.name,
                    iconKey = ItemIcon.FLAG.name,
                    category = Category.GOAL,
                    date = LocalDate.of(2026, 8, 7),
                    countMode = CountMode.DPLUS,
                    countFirstDayAsOne = true,
                ),
            ownerUserId = "user-me",
            updatedBy = "user-me",
            createdAt = 0L,
            updatedAt = 0L,
        )

    private val repository = FakeItemRepository(initial = listOf(quitSmoking))
    private val coupleRepository = FakeCoupleRepository()
    private val images = FakeImageRepository()
    private val quotas = FakeImageQuotaRepository()

    private fun viewModel(
        itemId: String? = null,
        type: ItemType = ItemType.DDAY,
        presetDate: String? = null,
    ) = ItemEditViewModel(ItemEditKey(itemId, type, presetDate), repository, coupleRepository, images, quotas, clock)

    private fun connect() {
        coupleRepository.couple.value = Couple("couple-1", "user-jimin", LocalDate.of(2025, 6, 14), true)
    }

    @Test
    fun `새 디데이는 오늘 날짜로 시작하고 제목이 비어 있으면 저장 버튼 비활성`() {
        val state = viewModel().state.value

        assertEquals(today, state.date)
        assertEquals(CountMode.DDAY, state.countMode)
        assertFalse(state.canSave)
    }

    @Test
    fun `캘린더에서 고른 날짜로 일정 추가를 시작함`() {
        val state = viewModel(type = ItemType.PLAN, presetDate = "2026-11-06").state.value

        assertEquals(LocalDate.of(2026, 11, 6), state.date)
    }

    @Test
    fun `빠른 시작으로 열면 카테고리 제목 아이콘이 채워져 바로 저장 가능`() {
        val state =
            ItemEditViewModel(
                ItemEditKey(null, ItemType.DDAY, null, presetCategory = Category.GOAL, presetTitle = "전역일"),
                repository,
                coupleRepository,
                images,
                quotas,
                clock,
            ).state.value

        assertEquals(Category.GOAL, state.category)
        assertEquals("전역일", state.title)
        assertEquals(ItemIcon.SCHOOL, state.icon)
        assertFalse(state.isIconChosen)
        assertTrue(state.canSave)
    }

    @Test
    fun `새 일정은 날짜 없이 시작하고 제목만 있으면 저장 가능`() {
        val viewModel = viewModel(type = ItemType.PLAN)

        viewModel.onAction(ItemEditAction.TitleChanged("하고 싶은 것"))

        assertNull(viewModel.state.value.date)
        assertTrue(viewModel.state.value.canSave)
    }

    @Test
    fun `디데이를 저장하면 알림 대표 고정까지 저장되고 뒤로 감`() =
        runTest {
            val viewModel = viewModel(presetDate = "2027-03-19")
            viewModel.onAction(ItemEditAction.TitleChanged(" 전역일 "))
            viewModel.onAction(ItemEditAction.PinnedChanged(true))

            viewModel.event.test {
                viewModel.onAction(ItemEditAction.ReminderToggled(7))
                viewModel.onAction(ItemEditAction.ReminderToggled(0))
                assertEquals(ItemEditEvent.RequestNotificationPermission, awaitItem())
                assertEquals(ItemEditEvent.RequestNotificationPermission, awaitItem())

                viewModel.onAction(ItemEditAction.SaveClicked)
                assertEquals(ItemEditEvent.NavigateBack, awaitItem())
            }

            val saved = repository.observeItems().first().single { it.id != quitSmoking.id }
            assertEquals("전역일", saved.content.title)
            assertEquals(LocalDate.of(2027, 3, 19), saved.content.date)
            assertEquals(CountMode.DDAY, saved.content.countMode)
            assertEquals(
                listOf(ItemReminder(0, ItemReminder.DefaultTime), ItemReminder(7, ItemReminder.DefaultTime)),
                repository.observeReminders(saved.id).first(),
            )
            assertEquals(saved.id, repository.observePinnedItemId().first())
        }

    @Test
    fun `알림을 켜면 알림 권한을 요청하고 끄면 요청하지 않음`() =
        runTest {
            val viewModel = viewModel(presetDate = "2027-03-19")

            viewModel.event.test {
                viewModel.onAction(ItemEditAction.ReminderToggled(1))
                assertEquals(ItemEditEvent.RequestNotificationPermission, awaitItem())

                viewModel.onAction(ItemEditAction.ReminderToggled(1))
                expectNoEvents()
            }
            assertEquals(emptySet<Int>(), viewModel.state.value.reminderOffsets)
        }

    @Test
    fun `저장에 실패하면 에러를 알리고 다시 저장할 수 있음`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onAction(ItemEditAction.TitleChanged("전역일"))
            repository.failNext = true

            viewModel.event.test {
                viewModel.onAction(ItemEditAction.SaveClicked)
                assertEquals(ItemEditEvent.SaveFailed, awaitItem())
            }

            assertFalse(viewModel.state.value.isSaving)
            assertTrue(viewModel.state.value.canSave)
        }

    @Test
    fun `수정으로 들어오면 저장된 값과 알림 대표 고정을 불러옴`() =
        runTest {
            repository.setReminders(quitSmoking.id, listOf(ItemReminder(1, ItemReminder.DefaultTime)))
            repository.setPinned(quitSmoking.id, pinned = true)

            val state = viewModel(itemId = quitSmoking.id).state.value

            assertFalse(state.isLoading)
            assertEquals("금연", state.title)
            assertEquals(ItemIcon.FLAG, state.icon)
            assertEquals(AppThemeType.SAGE, state.color)
            assertEquals(CountMode.DPLUS, state.countMode)
            assertTrue(state.countFirstDayAsOne)
            assertEquals(setOf(1), state.reminderOffsets)
            assertTrue(state.isPinned)
        }

    @Test
    fun `없는 항목을 수정하려 하면 뒤로 감`() =
        runTest {
            val viewModel = viewModel(itemId = "missing")

            viewModel.event.test { assertEquals(ItemEditEvent.NavigateBack, awaitItem()) }
        }

    @Test
    fun `수정해도 작성일과 화면에 없는 값은 유지됨`() =
        runTest {
            val viewModel = viewModel(itemId = quitSmoking.id)
            viewModel.onAction(ItemEditAction.TitleChanged("담배 끊은 날"))

            viewModel.event.test {
                viewModel.onAction(ItemEditAction.SaveClicked)
                assertEquals(ItemEditEvent.NavigateBack, awaitItem())
            }

            val saved = repository.observeItem(quitSmoking.id).first()!!
            assertEquals(quitSmoking.content.copy(title = "담배 끊은 날"), saved.content)
            assertEquals(quitSmoking.createdAt, saved.createdAt)
        }

    @Test
    fun `D-Day 방식으로 바꾸면 1일 세기가 꺼짐`() {
        val viewModel = viewModel()
        viewModel.onAction(ItemEditAction.CountModeSelected(CountMode.DPLUS))
        viewModel.onAction(ItemEditAction.CountFirstDayChanged(true))

        viewModel.onAction(ItemEditAction.CountModeSelected(CountMode.DDAY))

        assertFalse(viewModel.state.value.countFirstDayAsOne)
    }

    @Test
    fun `카테고리를 고르면 아이콘이 따라가지만 직접 고른 아이콘은 유지됨`() {
        val viewModel = viewModel()

        viewModel.onAction(ItemEditAction.CategorySelected(Category.BIRTHDAY))
        assertEquals(ItemIcon.CAKE, viewModel.state.value.icon)

        viewModel.onAction(ItemEditAction.IconSelected(ItemIcon.GIFT))
        viewModel.onAction(ItemEditAction.CategorySelected(Category.TRAVEL))
        assertEquals(ItemIcon.GIFT, viewModel.state.value.icon)
    }

    @Test
    fun `시간 지정을 누르면 시각 선택이 열리고 고르면 시간 지정으로 바뀜`() {
        val viewModel = viewModel(type = ItemType.PLAN)
        viewModel.onAction(ItemEditAction.TimeSlotSelected(TimeSlot.EVENING))

        viewModel.onAction(ItemEditAction.TimeSlotSelected(TimeSlot.EXACT))
        assertEquals(ItemEditDialog.TIME, viewModel.state.value.dialog)
        assertEquals(TimeSlot.EVENING, viewModel.state.value.timeSlot)

        viewModel.onAction(ItemEditAction.TimeSelected(LocalTime.of(19, 0)))
        assertNull(viewModel.state.value.dialog)
        assertEquals(TimeSlot.EXACT, viewModel.state.value.timeSlot)
        assertEquals(LocalTime.of(19, 0), viewModel.state.value.time)

        viewModel.onAction(ItemEditAction.TimeSlotSelected(TimeSlot.LUNCH))
        assertNull(viewModel.state.value.time)
    }

    @Test
    fun `일정을 저장하면 디데이 전용 값은 비우고 대표 고정은 건드리지 않음`() =
        runTest {
            repository.setPinned(quitSmoking.id, pinned = true)
            val viewModel = viewModel(type = ItemType.PLAN, presetDate = "2026-11-06")
            viewModel.onAction(ItemEditAction.TitleChanged("성수 저녁"))
            viewModel.onAction(ItemEditAction.RepeatSelected(RepeatRule.YEARLY))
            viewModel.onAction(ItemEditAction.TimeSlotSelected(TimeSlot.EVENING))
            viewModel.onAction(ItemEditAction.PlaceChanged("  "))

            viewModel.event.test {
                viewModel.onAction(ItemEditAction.SaveClicked)
                assertEquals(ItemEditEvent.NavigateBack, awaitItem())
            }

            val saved =
                repository
                    .observeItems()
                    .first()
                    .single { it.content.type == ItemType.PLAN }
                    .content
            assertNull(saved.countMode)
            assertEquals(RepeatRule.NONE, saved.repeatRule)
            assertEquals(TimeSlot.EVENING, saved.timeSlot)
            assertNull(saved.place)
            assertEquals(quitSmoking.id, repository.observePinnedItemId().first())
        }

    @Test
    fun `삭제를 확인하면 지우고 뒤로 감`() =
        runTest {
            val viewModel = viewModel(itemId = quitSmoking.id)
            viewModel.onAction(ItemEditAction.DeleteClicked)
            assertEquals(ItemEditDialog.DELETE_CONFIRM, viewModel.state.value.dialog)

            viewModel.event.test {
                viewModel.onAction(ItemEditAction.DeleteConfirmed)
                assertEquals(ItemEditEvent.NavigateBack, awaitItem())
            }

            assertNull(repository.observeItem(quitSmoking.id).first())
        }

    @Test
    fun `삭제에 실패하면 에러를 알리고 화면에 남음`() =
        runTest {
            val viewModel = viewModel(itemId = quitSmoking.id)
            repository.failNext = true

            viewModel.event.test {
                viewModel.onAction(ItemEditAction.DeleteConfirmed)
                assertEquals(ItemEditEvent.DeleteFailed, awaitItem())
            }

            assertFalse(viewModel.state.value.isSaving)
        }

    @Test
    fun `솔로면 공개 범위를 고르지 않고 나만 보기로 저장`() =
        runTest {
            val viewModel = viewModel()
            assertFalse(viewModel.state.value.showVisibility)

            viewModel.onAction(ItemEditAction.TitleChanged("전역일"))
            viewModel.onAction(ItemEditAction.SaveClicked)

            val saved = repository.observeItems().first().first { it.content.title == "전역일" }
            assertEquals(Visibility.PRIVATE, saved.content.visibility)
            assertNull(saved.content.coupleId)
        }

    @Test
    fun `커플이면 새 항목은 우리 둘이가 기본이고 커플 id와 함께 저장`() =
        runTest {
            connect()
            val viewModel = viewModel()
            assertTrue(viewModel.state.value.showVisibility)
            assertEquals(Visibility.SHARED, viewModel.state.value.visibility)

            viewModel.onAction(ItemEditAction.TitleChanged("제주 여행"))
            viewModel.onAction(ItemEditAction.SaveClicked)

            val saved = repository.observeItems().first().first { it.content.title == "제주 여행" }
            assertEquals(Visibility.SHARED, saved.content.visibility)
            assertEquals("couple-1", saved.content.coupleId)
        }

    @Test
    fun `커플이어도 나만 보기를 고르면 커플 id 없이 저장`() =
        runTest {
            connect()
            val viewModel = viewModel()

            viewModel.onAction(ItemEditAction.VisibilitySelected(Visibility.PRIVATE))
            viewModel.onAction(ItemEditAction.TitleChanged("토익 시험"))
            viewModel.onAction(ItemEditAction.SaveClicked)

            val saved = repository.observeItems().first().first { it.content.title == "토익 시험" }
            assertEquals(Visibility.PRIVATE, saved.content.visibility)
            assertNull(saved.content.coupleId)
        }

    @Test
    fun `기존 나만 보기 항목은 수정할 때 우리 둘이로 바꿀 수 있음`() =
        runTest {
            connect()
            val viewModel = viewModel(itemId = quitSmoking.id)
            assertEquals(Visibility.PRIVATE, viewModel.state.value.visibility)
            assertFalse(viewModel.state.value.isVisibilityLocked)

            viewModel.onAction(ItemEditAction.VisibilitySelected(Visibility.SHARED))
            viewModel.onAction(ItemEditAction.SaveClicked)

            val saved = repository.observeItem(quitSmoking.id).first()
            assertEquals(Visibility.SHARED, saved?.content?.visibility)
            assertEquals("couple-1", saved?.content?.coupleId)
        }

    @Test
    fun `공유한 항목은 잠겨 있어 나만 보기를 골라도 바뀌지 않음`() =
        runTest {
            connect()
            val sharedContent = quitSmoking.content.copy(visibility = Visibility.SHARED, coupleId = "couple-1")
            val sharedId = repository.saveItem(id = null, content = sharedContent).getOrThrow().id
            val viewModel = viewModel(itemId = sharedId)
            assertTrue(viewModel.state.value.isVisibilityLocked)

            viewModel.onAction(ItemEditAction.VisibilitySelected(Visibility.PRIVATE))

            assertEquals(Visibility.SHARED, viewModel.state.value.visibility)
        }

    @Test
    fun `커버 사진을 고르면 보여주고 저장하면 배경 사진 id가 들어감`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onAction(ItemEditAction.PhotoPicked("content://photo/1"))
            assertEquals(images.files.value["image-1"], viewModel.state.value.backgroundFile)

            viewModel.onAction(ItemEditAction.TitleChanged("전역일"))
            viewModel.onAction(ItemEditAction.SaveClicked)

            val saved = repository.observeItems().first().first { it.content.title == "전역일" }
            assertEquals("image-1", saved.content.backgroundImageId)
        }

    @Test
    fun `커버 사진을 바꿔 저장하면 이전 사진을 버리고 취소하면 새 사진만 버림`() =
        runTest {
            images.add("old")
            val withPhoto = quitSmoking.content.copy(backgroundImageId = "old")
            val id = repository.saveItem(id = null, content = withPhoto).getOrThrow().id

            val cancelled = viewModel(itemId = id)
            cancelled.onAction(ItemEditAction.PhotoPicked("content://photo/1"))
            cancelled.onAction(ItemEditAction.CancelClicked)
            assertEquals(listOf("image-1"), images.discarded)
            assertEquals(
                "old",
                repository
                    .observeItem(id)
                    .first()
                    ?.content
                    ?.backgroundImageId,
            )

            val edited = viewModel(itemId = id)
            edited.onAction(ItemEditAction.PhotoPicked("content://photo/2"))
            edited.onAction(ItemEditAction.SaveClicked)
            assertEquals(listOf("image-1", "old"), images.discarded)
            assertEquals(
                "image-2",
                repository
                    .observeItem(id)
                    .first()
                    ?.content
                    ?.backgroundImageId,
            )
        }

    @Test
    fun `일정은 커버 사진을 저장하지 않음`() =
        runTest {
            val viewModel = viewModel(type = ItemType.PLAN)
            viewModel.onAction(ItemEditAction.PhotoPicked("content://photo/1"))
            viewModel.onAction(ItemEditAction.TitleChanged("성수 저녁"))
            viewModel.onAction(ItemEditAction.SaveClicked)

            assertNull(
                repository
                    .observeItems()
                    .first()
                    .first { it.content.title == "성수 저녁" }
                    .content.backgroundImageId,
            )
        }

    @Test
    fun `사진을 읽지 못하면 알림`() =
        runTest {
            val viewModel = viewModel()
            images.failNextImport = true

            viewModel.event.test {
                viewModel.onAction(ItemEditAction.PhotoPicked("content://broken"))
                assertEquals(ItemEditEvent.PhotoFailed, awaitItem())
            }
            assertNull(viewModel.state.value.backgroundImageId)
        }

    private val fullQuota = ImageUploadQuota("user-me", "couple-1", used = 30, bonus = 0)

    @Test
    fun `우리 둘이 디데이는 남은 사진 수를 보여주고 나만 보기로 바꾸면 숨김`() =
        runTest {
            connect()
            quotas.quota = ImageUploadQuota("user-me", "couple-1", used = 18, bonus = 0)
            val viewModel = viewModel()
            assertEquals(12, viewModel.state.value.photoQuotaRemaining)

            viewModel.onAction(ItemEditAction.VisibilitySelected(Visibility.PRIVATE))
            assertNull(viewModel.state.value.photoQuotaRemaining)
        }

    @Test
    fun `솔로 · 일정 · 한도 읽기 실패면 남은 사진 수를 보여주지 않음`() =
        runTest {
            quotas.quota = ImageUploadQuota("user-me", "couple-1", used = 18, bonus = 0)
            assertNull(viewModel().state.value.photoQuotaRemaining)

            connect()
            assertNull(viewModel(type = ItemType.PLAN).state.value.photoQuotaRemaining)

            quotas.failQuota = true
            assertNull(viewModel().state.value.photoQuotaRemaining)
        }

    @Test
    fun `광고 보상을 받으면 남은 사진 수가 늘어남`() =
        runTest {
            connect()
            quotas.quota = fullQuota
            val viewModel = viewModel()
            assertEquals(0, viewModel.state.value.photoQuotaRemaining)
            viewModel.event.test {
                viewModel.onAction(ItemEditAction.PhotoClicked)
                viewModel.onAction(ItemEditAction.WatchAdClicked)
                awaitItem()
                viewModel.onAction(ItemEditAction.RewardedAdFinished(RewardedAdResult.Earned))
                awaitItem()
            }
            assertEquals(1, viewModel.state.value.photoQuotaRemaining)
        }

    @Test
    fun `나만 보기 항목은 사진 한도를 묻지 않고 사진 선택기를 엶`() =
        runTest {
            connect()
            quotas.quota = fullQuota
            val viewModel = viewModel()
            viewModel.onAction(ItemEditAction.VisibilitySelected(Visibility.PRIVATE))
            viewModel.event.test {
                viewModel.onAction(ItemEditAction.PhotoClicked)
                assertEquals(ItemEditEvent.OpenPhotoPicker, awaitItem())
            }
            assertNull(viewModel.state.value.imageLimit)
        }

    @Test
    fun `공유 항목인데 커플 사진 한도를 다 썼으면 시트를 띄우고 광고를 보면 사진 선택기를 엶`() =
        runTest {
            connect()
            quotas.quota = fullQuota
            val viewModel = viewModel()
            viewModel.event.test {
                viewModel.onAction(ItemEditAction.PhotoClicked)
                assertEquals(fullQuota, viewModel.state.value.imageLimit)

                viewModel.onAction(ItemEditAction.WatchAdClicked)
                assertEquals(ItemEditEvent.ShowRewardedAd(RewardedAdRequest("user-me", "couple-1")), awaitItem())

                viewModel.onAction(ItemEditAction.RewardedAdFinished(RewardedAdResult.Earned))
                assertEquals(ItemEditEvent.OpenPhotoPicker, awaitItem())
            }
            assertNull(viewModel.state.value.imageLimit)
            assertFalse(viewModel.state.value.isRewardWorking)
        }

    @Test
    fun `광고를 불러오지 못하면 알리고 시트에 남음`() =
        runTest {
            connect()
            quotas.quota = fullQuota
            val viewModel = viewModel()
            viewModel.event.test {
                viewModel.onAction(ItemEditAction.PhotoClicked)
                viewModel.onAction(ItemEditAction.WatchAdClicked)
                awaitItem()
                viewModel.onAction(ItemEditAction.RewardedAdFinished(RewardedAdResult.Failed))
                assertEquals(ItemEditEvent.RewardedAdFailed, awaitItem())
            }
            assertEquals(fullQuota, viewModel.state.value.imageLimit)
        }
}
