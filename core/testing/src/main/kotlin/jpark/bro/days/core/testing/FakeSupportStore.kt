package jpark.bro.days.core.testing

import jpark.bro.days.core.billing.SupportOffer
import jpark.bro.days.core.billing.SupportStore

/** 후원 상품 Fake. [failOffers]면 상품 조회가 실패한다 (Play 연결 실패) */
class FakeSupportStore(
    var offers: List<SupportOffer> = emptyList(),
) : SupportStore {
    var failOffers: Boolean = false
    var consumePendingCalls: Int = 0

    override suspend fun offers(): Result<List<SupportOffer>> =
        if (failOffers) Result.failure(IllegalStateException("Play 연결 실패 (테스트)")) else Result.success(offers)

    override suspend fun consumePending(): Result<Int> {
        consumePendingCalls++
        return Result.success(0)
    }
}
