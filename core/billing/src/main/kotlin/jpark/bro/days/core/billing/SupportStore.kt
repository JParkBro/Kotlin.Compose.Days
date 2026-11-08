package jpark.bro.days.core.billing

/**
 * 개발자 응원하기 상품 (ads-strategy.md 6장). 혜택 없는 소모형 인앱 상품이고 가격은 Play Console에서 정한다
 * (₩1,200 / ₩3,300 / ₩6,600). 상품 id는 Play Console에 같은 id로 등록한다.
 */
enum class SupportProduct(
    val productId: String,
) {
    SMALL("support_small"),
    MEDIUM("support_medium"),
    LARGE("support_large"),
}

/** Play에 등록된 상품과 사용자 통화로 바뀐 가격 (예: "₩1,200") */
data class SupportOffer(
    val product: SupportProduct,
    val price: String,
)

enum class SupportPurchaseResult {
    /** 결제 · 소모까지 끝남 */
    Purchased,

    /** 결제가 보류됨 (편의점 결제 등). 결제되면 다음에 [SupportStore.consumePending]이 소모한다 */
    Pending,

    /** 사용자가 결제 창을 닫음 */
    Canceled,

    Failed,
}

/** 화면(ViewModel)이 쓰는 부분. 결제 창은 Activity가 필요해 Root가 [rememberSupportPurchaseLauncher]로 띄운다 */
interface SupportStore {
    /** 등록된 상품만 [SupportProduct] 순서로. 결제를 쓸 수 없으면 실패 */
    suspend fun offers(): Result<List<SupportOffer>>

    /** 결제됐지만 아직 소모하지 않은 후원(결제 중 앱 종료 · 보류 후 결제)을 소모한다. 소모한 수 */
    suspend fun consumePending(): Result<Int>
}
