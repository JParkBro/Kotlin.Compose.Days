package jpark.bro.days.feature.settings.impl.supportdeveloper

import app.cash.turbine.test
import jpark.bro.days.core.billing.SupportOffer
import jpark.bro.days.core.billing.SupportProduct
import jpark.bro.days.core.billing.SupportPurchaseResult
import jpark.bro.days.core.testing.FakeSupportStore
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SupportDeveloperViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val offers =
        listOf(
            SupportOffer(SupportProduct.SMALL, "₩1,200"),
            SupportOffer(SupportProduct.MEDIUM, "₩3,300"),
            SupportOffer(SupportProduct.LARGE, "₩6,600"),
        )
    private val store = FakeSupportStore(offers)

    @Test
    fun `열면 남은 후원을 소모하고 상품과 가격을 보여줌`() {
        val state = SupportDeveloperViewModel(store).state.value
        assertEquals(offers, state.offers)
        assertFalse(state.isLoading)
        assertEquals(1, store.consumePendingCalls)
    }

    @Test
    fun `Play에 연결하지 못하면 실패를 보여주고 다시 시도할 수 있음`() {
        store.failOffers = true
        val viewModel = SupportDeveloperViewModel(store)
        assertTrue(viewModel.state.value.isLoadFailed)

        store.failOffers = false
        viewModel.onAction(SupportDeveloperAction.RetryClicked)
        assertFalse(viewModel.state.value.isLoadFailed)
        assertEquals(offers, viewModel.state.value.offers)
    }

    @Test
    fun `상품을 누르면 결제 창을 띄우고 결제되면 감사 인사`() =
        runTest {
            val viewModel = SupportDeveloperViewModel(store)
            viewModel.event.test {
                viewModel.onAction(SupportDeveloperAction.ProductClicked(SupportProduct.MEDIUM))
                assertEquals(SupportDeveloperEvent.LaunchPurchase(SupportProduct.MEDIUM), awaitItem())
                assertTrue(viewModel.state.value.isPurchasing)

                viewModel.onAction(SupportDeveloperAction.ProductClicked(SupportProduct.LARGE))
                expectNoEvents()

                viewModel.onAction(SupportDeveloperAction.PurchaseFinished(SupportPurchaseResult.Purchased))
                assertEquals(SupportDeveloperEvent.Thanks, awaitItem())
            }
            assertFalse(viewModel.state.value.isPurchasing)
        }

    @Test
    fun `보류 · 실패는 알리고 취소는 조용히 끝냄`() =
        runTest {
            val viewModel = SupportDeveloperViewModel(store)
            viewModel.event.test {
                viewModel.onAction(SupportDeveloperAction.PurchaseFinished(SupportPurchaseResult.Pending))
                assertEquals(SupportDeveloperEvent.PurchasePending, awaitItem())
                viewModel.onAction(SupportDeveloperAction.PurchaseFinished(SupportPurchaseResult.Failed))
                assertEquals(SupportDeveloperEvent.PurchaseFailed, awaitItem())
                viewModel.onAction(SupportDeveloperAction.PurchaseFinished(SupportPurchaseResult.Canceled))
                expectNoEvents()
            }
        }
}
