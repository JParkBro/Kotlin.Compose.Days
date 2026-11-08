package jpark.bro.days.feature.settings.impl.addebug

import app.cash.turbine.test
import jpark.bro.days.core.ads.RewardedAdRequest
import jpark.bro.days.core.ads.RewardedAdResult
import jpark.bro.days.core.model.ImageUploadQuota
import jpark.bro.days.core.testing.FakeImageQuotaRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class AdDebugViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val quota = ImageUploadQuota("user-me", "couple-1", used = 12, bonus = 0)
    private val quotas = FakeImageQuotaRepository(quota)

    @Test
    fun `열면 커플 사진 한도를 보여줌`() {
        val viewModel = AdDebugViewModel(quotas)
        assertEquals(quota, viewModel.state.value.quota)
        assertNull(viewModel.state.value.message)
    }

    @Test
    fun `커플이 아니거나 읽지 못하면 이유를 보여줌`() {
        assertEquals(AdDebugMessage.NOT_COUPLED, AdDebugViewModel(FakeImageQuotaRepository()).state.value.message)

        quotas.failQuota = true
        assertEquals(AdDebugMessage.LOAD_FAILED, AdDebugViewModel(quotas).state.value.message)
    }

    @Test
    fun `한도가 남아 있어도 광고를 띄우고 끝까지 보면 늘어난 한도를 보여줌`() =
        runTest {
            val viewModel = AdDebugViewModel(quotas)
            viewModel.event.test {
                viewModel.onAction(AdDebugAction.WatchAdClicked)
                assertEquals(AdDebugEvent.ShowRewardedAd(RewardedAdRequest("user-me", "couple-1")), awaitItem())
            }
            viewModel.onAction(AdDebugAction.RewardedAdFinished(RewardedAdResult.Earned))

            val state = viewModel.state.value
            assertEquals(1, state.quota?.bonus)
            assertEquals(AdDebugMessage.REWARD_GRANTED, state.message)
            assertFalse(state.isWatching)
        }

    @Test
    fun `보상이 늦거나 광고를 닫거나 실패하면 결과를 알림`() {
        val viewModel = AdDebugViewModel(quotas)
        quotas.grantReward = false

        viewModel.onAction(AdDebugAction.WatchAdClicked)
        viewModel.onAction(AdDebugAction.RewardedAdFinished(RewardedAdResult.Earned))
        assertEquals(AdDebugMessage.REWARD_PENDING, viewModel.state.value.message)

        viewModel.onAction(AdDebugAction.WatchAdClicked)
        viewModel.onAction(AdDebugAction.RewardedAdFinished(RewardedAdResult.Dismissed))
        assertEquals(AdDebugMessage.AD_DISMISSED, viewModel.state.value.message)

        viewModel.onAction(AdDebugAction.WatchAdClicked)
        viewModel.onAction(AdDebugAction.RewardedAdFinished(RewardedAdResult.Failed))
        assertEquals(AdDebugMessage.AD_FAILED, viewModel.state.value.message)
        assertEquals(
            0,
            viewModel.state.value.quota
                ?.bonus,
        )
    }
}
