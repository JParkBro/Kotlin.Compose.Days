package jpark.bro.days.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import jpark.bro.days.core.common.suspendRunCatching
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/**
 * Play Billing으로 후원 상품을 판다. 혜택이 없으므로 서버 검증 없이 결제되면 바로 소모한다
 * (소모하지 않으면 Play가 3일 뒤 환불하고, 같은 상품을 다시 살 수 없다).
 */
class PlaySupportStore(
    context: Context,
) : SupportStore {
    private var purchaseResult: CompletableDeferred<SupportPurchaseResult>? = null

    private val client: BillingClient =
        BillingClient
            .newBuilder(context.applicationContext)
            .setListener { result, purchases -> onPurchasesUpdated(result, purchases.orEmpty()) }
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection()
            .build()
    private val connectLock = Mutex()

    override suspend fun offers(): Result<List<SupportOffer>> =
        suspendRunCatching {
            productDetails()
                .mapNotNull { details ->
                    val product = SupportProduct.entries.find { it.productId == details.productId }
                    val price = details.oneTimePurchaseOfferDetails?.formattedPrice
                    if (product != null && price != null) SupportOffer(product, price) else null
                }.sortedBy { it.product.ordinal }
        }

    override suspend fun consumePending(): Result<Int> =
        suspendRunCatching {
            connect()
            val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
            val result = client.queryPurchasesAsync(params)
            check(result.billingResult.isOk) { "구매 내역 조회 실패 (${result.billingResult.responseCode})" }
            result.purchasesList
                .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && it.isSupportProduct() }
                .count { consume(it) }
        }

    /** 결제 창을 띄우고 끝날 때까지 기다린다. 메인 스레드에서 부른다 */
    suspend fun purchase(
        activity: Activity,
        product: SupportProduct,
    ): SupportPurchaseResult {
        val details =
            suspendRunCatching { productDetails().find { it.productId == product.productId } }.getOrNull()
        return if (details == null) SupportPurchaseResult.Failed else launch(activity, details)
    }

    private suspend fun launch(
        activity: Activity,
        details: ProductDetails,
    ): SupportPurchaseResult {
        val deferred = CompletableDeferred<SupportPurchaseResult>().also { purchaseResult = it }
        val params =
            BillingFlowParams
                .newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams
                            .newBuilder()
                            .setProductDetails(details)
                            .build(),
                    ),
                ).build()
        val launched = client.launchBillingFlow(activity, params)
        if (launched.isOk) return deferred.await()
        // 결제 창을 띄우지 못함: 리스너가 부르지 않으므로 여기서 끝낸다
        purchaseResult = null
        return if (launched.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            SupportPurchaseResult.Canceled
        } else {
            SupportPurchaseResult.Failed
        }
    }

    private fun onPurchasesUpdated(
        result: BillingResult,
        purchases: List<Purchase>,
    ) {
        val deferred = purchaseResult ?: return
        purchaseResult = null
        when {
            result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED -> {
                deferred.complete(SupportPurchaseResult.Canceled)
            }

            !result.isOk -> {
                deferred.complete(SupportPurchaseResult.Failed)
            }

            purchases.any { it.purchaseState == Purchase.PurchaseState.PENDING } -> {
                deferred.complete(SupportPurchaseResult.Pending)
            }

            else -> {
                deferred.complete(SupportPurchaseResult.Purchased)
            }
        }
    }

    private suspend fun productDetails(): List<ProductDetails> {
        connect()
        val products =
            SupportProduct.entries.map {
                QueryProductDetailsParams.Product
                    .newBuilder()
                    .setProductId(it.productId)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            }
        val result = client.queryProductDetails(QueryProductDetailsParams.newBuilder().setProductList(products).build())
        check(result.billingResult.isOk) { "상품 조회 실패 (${result.billingResult.responseCode})" }
        return result.productDetailsList.orEmpty()
    }

    private suspend fun consume(purchase: Purchase): Boolean {
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        return client.consumePurchase(params).billingResult.isOk
    }

    private suspend fun connect() {
        connectLock.withLock {
            if (client.isReady) return
            val result =
                suspendCancellableCoroutine { continuation ->
                    client.startConnection(
                        object : BillingClientStateListener {
                            override fun onBillingSetupFinished(result: BillingResult) {
                                if (continuation.isActive) continuation.resume(result)
                            }

                            override fun onBillingServiceDisconnected() = Unit
                        },
                    )
                }
            check(result.isOk) { "Play 결제에 연결하지 못함 (${result.responseCode})" }
        }
    }

    private fun Purchase.isSupportProduct() = products.any { id -> SupportProduct.entries.any { it.productId == id } }

    private val BillingResult.isOk get() = responseCode == BillingClient.BillingResponseCode.OK
}
