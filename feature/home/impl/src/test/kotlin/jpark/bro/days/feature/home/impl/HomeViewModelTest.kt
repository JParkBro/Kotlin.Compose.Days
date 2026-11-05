package jpark.bro.days.feature.home.impl

import app.cash.turbine.test
import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.Couple
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.UserProfile
import jpark.bro.days.core.model.Visibility
import jpark.bro.days.core.testing.FakeAnniversaryRepository
import jpark.bro.days.core.testing.FakeCoupleRepository
import jpark.bro.days.core.testing.FakeImageRepository
import jpark.bro.days.core.testing.FakeItemRepository
import jpark.bro.days.core.testing.FakeProfileRepository
import jpark.bro.days.core.testing.FakeSettingsRepository
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
import java.time.ZoneId

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val seoul = ZoneId.of("Asia/Seoul")
    private val clock = Clock.fixed(Today.atStartOfDay(seoul).toInstant(), seoul)
    private val settingsRepository = FakeSettingsRepository()
    private val coupleRepository = FakeCoupleRepository()
    private val profileRepository =
        FakeProfileRepository(
            me = UserProfile("user-me", "현우", LocalDate.of(1998, 4, 12)),
            partner = UserProfile("user-jimin", "지민", LocalDate.of(1999, 9, 28)),
        )
    private val anniversaryRepository = FakeAnniversaryRepository()
    private val images = FakeImageRepository()
    private lateinit var itemRepository: FakeItemRepository

    private fun viewModel(items: List<Item> = emptyList()): HomeViewModel {
        itemRepository = FakeItemRepository(items)
        return HomeViewModel(
            itemRepository,
            settingsRepository,
            HomeCoupleSource(coupleRepository, profileRepository, anniversaryRepository, images),
            clock,
        )
    }

    /** 현우 · 지민, 사귄 날 2025.06.14 (1일 세기) */
    private fun connect() {
        coupleRepository.couple.value = Couple("couple-1", "user-jimin", LocalDate.of(2025, 6, 14), true)
    }

    private fun Item.shared() = copy(content = content.copy(visibility = Visibility.SHARED, coupleId = "couple-1"))

    @Test
    fun `등록한 항목이 없으면 빈 상태와 커플 연결 카드를 보여줌`() {
        val state = viewModel().state.value

        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
        assertTrue(state.showCoupleBanner)
    }

    @Test
    fun `항목이 있으면 오늘 기준으로 히어로와 리스트를 만듦`() {
        val state = viewModel(listOf(Discharge, MomBirthday, SeongsuDinner)).state.value

        assertFalse(state.isEmpty)
        assertEquals(Today, state.today)
        assertEquals(
            DdayCount.Remaining(58),
            state.content.hero
                ?.entry
                ?.dday,
        )
        assertEquals(listOf(Discharge), state.content.rows.map { it.item })
        assertEquals(SeongsuDinner, state.content.nextPlan)
    }

    @Test
    fun `대표를 고정하면 히어로가 바뀜`() =
        runTest {
            val viewModel = viewModel(listOf(Discharge, MomBirthday))

            itemRepository.setPinned(Discharge.id, pinned = true)

            assertEquals(
                Discharge,
                viewModel.state.value.content.hero
                    ?.entry
                    ?.item,
            )
            assertTrue(
                viewModel.state.value.content.hero!!
                    .isPinned,
            )
        }

    @Test
    fun `디데이를 누르면 상세로 감`() =
        runTest {
            val viewModel = viewModel(listOf(Discharge))

            viewModel.event.test {
                viewModel.onAction(HomeAction.ItemClicked(Discharge.id, ItemType.DDAY))
                assertEquals(HomeEvent.OpenItemDetail(Discharge.id), awaitItem())
            }
        }

    @Test
    fun `일정을 누르면 바로 수정 화면으로 감`() =
        runTest {
            val viewModel = viewModel(listOf(SeongsuDinner))

            viewModel.event.test {
                viewModel.onAction(HomeAction.ItemClicked(SeongsuDinner.id, ItemType.PLAN))
                assertEquals(HomeEvent.OpenItemEdit(ItemType.PLAN, itemId = SeongsuDinner.id), awaitItem())
            }
        }

    @Test
    fun `플러스 버튼은 추가 시트를 열고 종류를 고르면 시트를 닫고 추가 화면을 엶`() =
        runTest {
            val viewModel = viewModel(listOf(Discharge))
            viewModel.onAction(HomeAction.FabClicked)
            assertTrue(viewModel.state.value.isAddSheetOpen)

            viewModel.event.test {
                viewModel.onAction(HomeAction.AddClicked(ItemType.PLAN))
                assertEquals(HomeEvent.OpenItemEdit(ItemType.PLAN), awaitItem())
            }
            assertFalse(viewModel.state.value.isAddSheetOpen)
        }

    @Test
    fun `추가 시트를 닫으면 아무 데도 가지 않음`() {
        val viewModel = viewModel(listOf(Discharge))
        viewModel.onAction(HomeAction.FabClicked)

        viewModel.onAction(HomeAction.AddSheetDismissed)

        assertFalse(viewModel.state.value.isAddSheetOpen)
    }

    @Test
    fun `빠른 시작 칩을 누르면 그 카테고리로 디데이 추가를 엶`() =
        runTest {
            val viewModel = viewModel()

            viewModel.event.test {
                viewModel.onAction(HomeAction.QuickStartClicked(QuickStart.DISCHARGE))
                assertEquals(HomeEvent.OpenItemEdit(ItemType.DDAY, quickStart = QuickStart.DISCHARGE), awaitItem())
            }
        }

    @Test
    fun `연결하기를 누르면 커플 연결 화면으로 감`() =
        runTest {
            val viewModel = viewModel()

            viewModel.event.test {
                viewModel.onAction(HomeAction.ConnectCoupleClicked)
                assertEquals(HomeEvent.OpenPairing, awaitItem())
            }
        }

    @Test
    fun `커플 연결 카드를 닫으면 저장되고 다시 보이지 않음`() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(HomeAction.CoupleBannerDismissed)

            assertFalse(viewModel.state.value.showCoupleBanner)
            assertTrue(settingsRepository.observeCoupleBannerDismissed().first())
            assertFalse(viewModel(listOf(Discharge)).state.value.showCoupleBanner)
        }

    @Test
    fun `커플이면 함께한 날 다음 기념일을 보여주고 대표 히어로와 연결 카드는 없음`() {
        connect()
        val state = viewModel(listOf(Discharge, MomBirthday)).state.value

        val couple = state.content.couple!!
        assertEquals(510, couple.togetherDays)
        assertEquals("지민", couple.partnerName)
        assertEquals(AutoAnniversary.Days(600, LocalDate.of(2027, 2, 3)), couple.nextAnniversary?.anniversary)
        assertNull(state.content.hero)
        assertEquals(2, state.content.rows.size)
        assertFalse(state.showCoupleBanner)
    }

    @Test
    fun `숨긴 기념일은 빠지고 다음으로 가까운 생일이 다음 기념일`() {
        connect()
        anniversaryRepository.hidden.value = setOf("DAY_600")

        val next =
            viewModel(listOf(Discharge))
                .state.value.content.couple
                ?.nextAnniversary

        assertEquals(AutoAnniversary.Birthday("user-me", 2027, LocalDate.of(2027, 4, 12)), next?.anniversary)
    }

    @Test
    fun `필터로 우리 둘이 나만 보기 항목을 나눠 봄`() {
        connect()
        val sharedMom = MomBirthday.shared()
        val viewModel = viewModel(listOf(Discharge, sharedMom))

        viewModel.onAction(HomeAction.FilterSelected(HomeFilter.SHARED))
        assertEquals(
            listOf(sharedMom),
            viewModel.state.value.visibleRows
                .map { it.item },
        )

        viewModel.onAction(HomeAction.FilterSelected(HomeFilter.PRIVATE))
        assertEquals(
            listOf(Discharge),
            viewModel.state.value.visibleRows
                .map { it.item },
        )
    }

    @Test
    fun `연결이 끊기면 솔로 홈으로 돌아가고 필터를 쓰지 않음`() {
        connect()
        val viewModel = viewModel(listOf(Discharge, MomBirthday))
        viewModel.onAction(HomeAction.FilterSelected(HomeFilter.SHARED))

        coupleRepository.couple.value = null

        val state = viewModel.state.value
        assertNull(state.content.couple)
        assertEquals(
            MomBirthday,
            state.content.hero
                ?.entry
                ?.item,
        )
        assertEquals(listOf(Discharge), state.visibleRows.map { it.item })
        assertTrue(state.showCoupleBanner)
    }

    @Test
    fun `두 사람 프로필 사진이 있으면 히어로에 사진 파일을 줌`() {
        connect()
        val face = images.add("partner-face")
        profileRepository.partnerProfile.value =
            profileRepository.partnerProfile.value?.copy(profileImageId = "partner-face")

        val couple =
            viewModel(listOf(Discharge))
                .state.value.content.couple!!

        assertEquals(face, couple.partnerPhoto)
        assertNull(couple.myPhoto)
    }
}
