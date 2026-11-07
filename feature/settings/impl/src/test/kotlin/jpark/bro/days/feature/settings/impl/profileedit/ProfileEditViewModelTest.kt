package jpark.bro.days.feature.settings.impl.profileedit

import app.cash.turbine.test
import jpark.bro.days.core.ads.RewardedAdRequest
import jpark.bro.days.core.ads.RewardedAdResult
import jpark.bro.days.core.model.ImageUploadQuota
import jpark.bro.days.core.model.UserProfile
import jpark.bro.days.core.testing.FakeImageQuotaRepository
import jpark.bro.days.core.testing.FakeImageRepository
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
    private val images = FakeImageRepository()
    private val quotas = FakeImageQuotaRepository()
    private val viewModel by lazy { ProfileEditViewModel(repository, images, quotas, clock) }

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
        val empty = ProfileEditViewModel(FakeProfileRepository(), images, quotas, clock)

        assertEquals("", empty.state.value.nickname)
        assertFalse(empty.state.value.canSave)
    }

    @Test
    fun `사진을 누르면 사진 선택기를 엶`() =
        runTest {
            viewModel.event.test {
                viewModel.onAction(ProfileEditAction.PhotoClicked)
                assertEquals(ProfileEditEvent.OpenPhotoPicker, awaitItem())
            }
        }

    @Test
    fun `사진을 고르면 보여주고 저장하면 프로필에 사진 id가 들어감`() =
        runTest {
            viewModel.onAction(ProfileEditAction.PhotoPicked("content://photo/1"))

            val state = viewModel.state.value
            assertEquals("image-1", state.photoId)
            assertEquals(images.files.value["image-1"], state.photoFile)
            assertTrue(state.canSave)

            viewModel.onAction(ProfileEditAction.SaveClicked)
            assertEquals("image-1", repository.myProfile.value?.profileImageId)
        }

    @Test
    fun `사진을 바꿔 저장하면 이전 사진을 버림`() =
        runTest {
            images.add("old")
            repository.myProfile.value = saved.copy(profileImageId = "old")

            viewModel.onAction(ProfileEditAction.PhotoPicked("content://photo/1"))
            assertEquals(emptyList<String>(), images.discarded)
            viewModel.onAction(ProfileEditAction.SaveClicked)

            assertEquals(listOf("old"), images.discarded)
        }

    @Test
    fun `저장하지 않고 뒤로 가면 새로 고른 사진을 버리고 프로필은 그대로`() =
        runTest {
            viewModel.onAction(ProfileEditAction.PhotoPicked("content://photo/1"))

            viewModel.event.test {
                viewModel.onAction(ProfileEditAction.BackClicked)
                assertEquals(ProfileEditEvent.NavigateBack, awaitItem())
            }
            assertEquals(listOf("image-1"), images.discarded)
            assertNull(repository.myProfile.value?.profileImageId)
        }

    @Test
    fun `사진을 읽지 못하면 알리고 사진은 그대로`() =
        runTest {
            images.failNextImport = true

            viewModel.event.test {
                viewModel.onAction(ProfileEditAction.PhotoPicked("content://broken"))
                assertEquals(ProfileEditEvent.PhotoFailed, awaitItem())
            }
            assertNull(viewModel.state.value.photoId)
            assertFalse(viewModel.state.value.isPhotoWorking)
        }

    @Test
    fun `저장된 사진을 지우면 저장할 수 있고 저장하면 사진이 빠짐`() =
        runTest {
            images.add("old")
            repository.myProfile.value = saved.copy(profileImageId = "old")
            assertEquals(images.files.value["old"], viewModel.state.value.photoFile)

            viewModel.onAction(ProfileEditAction.PhotoRemoved)
            assertNull(viewModel.state.value.photoFile)
            viewModel.onAction(ProfileEditAction.SaveClicked)

            assertNull(repository.myProfile.value?.profileImageId)
            assertEquals(listOf("old"), images.discarded)
        }

    private val fullQuota = ImageUploadQuota("user-me", "couple-1", used = 30, bonus = 0)

    @Test
    fun `커플 사진 한도가 남아 있으면 바로 사진 선택기를 엶`() =
        runTest {
            quotas.quota = fullQuota.copy(used = 29)
            viewModel.event.test {
                viewModel.onAction(ProfileEditAction.PhotoClicked)
                assertEquals(ProfileEditEvent.OpenPhotoPicker, awaitItem())
            }
            assertNull(viewModel.state.value.imageLimit)
        }

    @Test
    fun `한도를 읽지 못하면 사진 선택기를 엶 (올릴 때 서버가 다시 막음)`() =
        runTest {
            quotas.failQuota = true
            viewModel.event.test {
                viewModel.onAction(ProfileEditAction.PhotoClicked)
                assertEquals(ProfileEditEvent.OpenPhotoPicker, awaitItem())
            }
        }

    @Test
    fun `한도를 다 썼으면 시트를 띄우고 광고를 끝까지 보면 늘어난 한도로 사진 선택기를 엶`() =
        runTest {
            quotas.quota = fullQuota
            viewModel.event.test {
                viewModel.onAction(ProfileEditAction.PhotoClicked)
                assertEquals(fullQuota, viewModel.state.value.imageLimit)

                viewModel.onAction(ProfileEditAction.WatchAdClicked)
                assertEquals(ProfileEditEvent.ShowRewardedAd(RewardedAdRequest("user-me", "couple-1")), awaitItem())
                assertTrue(viewModel.state.value.isRewardWorking)

                viewModel.onAction(ProfileEditAction.RewardedAdFinished(RewardedAdResult.Earned))
                assertEquals(ProfileEditEvent.OpenPhotoPicker, awaitItem())
            }
            val state = viewModel.state.value
            assertNull(state.imageLimit)
            assertFalse(state.isRewardWorking)
            assertEquals(1, quotas.quota?.bonus)
        }

    @Test
    fun `보상이 제때 반영되지 않으면 시트를 닫고 알림`() =
        runTest {
            quotas.quota = fullQuota
            quotas.grantReward = false
            viewModel.event.test {
                viewModel.onAction(ProfileEditAction.PhotoClicked)
                viewModel.onAction(ProfileEditAction.WatchAdClicked)
                awaitItem()
                viewModel.onAction(ProfileEditAction.RewardedAdFinished(RewardedAdResult.Earned))
                assertEquals(ProfileEditEvent.RewardPending, awaitItem())
            }
            assertNull(viewModel.state.value.imageLimit)
        }

    @Test
    fun `광고를 중간에 닫으면 시트에 남고, 불러오지 못하면 알림`() =
        runTest {
            quotas.quota = fullQuota
            viewModel.event.test {
                viewModel.onAction(ProfileEditAction.PhotoClicked)
                viewModel.onAction(ProfileEditAction.WatchAdClicked)
                awaitItem()
                viewModel.onAction(ProfileEditAction.RewardedAdFinished(RewardedAdResult.Dismissed))
                expectNoEvents()
                assertEquals(fullQuota, viewModel.state.value.imageLimit)
                assertFalse(viewModel.state.value.isRewardWorking)

                viewModel.onAction(ProfileEditAction.WatchAdClicked)
                awaitItem()
                viewModel.onAction(ProfileEditAction.RewardedAdFinished(RewardedAdResult.Failed))
                assertEquals(ProfileEditEvent.RewardedAdFailed, awaitItem())
            }
            viewModel.onAction(ProfileEditAction.ImageLimitDismissed)
            assertNull(viewModel.state.value.imageLimit)
        }
}
