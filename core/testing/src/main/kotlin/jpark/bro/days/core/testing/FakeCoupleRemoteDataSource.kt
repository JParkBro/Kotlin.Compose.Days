package jpark.bro.days.core.testing

import jpark.bro.days.core.firebase.sync.CoupleDto
import jpark.bro.days.core.firebase.sync.CoupleRemoteDataSource
import jpark.bro.days.core.firebase.sync.PairCodeDto

/** 서버 대신 메모리에 코드 · 커플 문서를 둔다. 규칙 검사는 하지 않는다 (규칙은 firebase/tests) */
class FakeCoupleRemoteDataSource : CoupleRemoteDataSource {
    val pairCodes = mutableMapOf<String, PairCodeDto>()
    val couples = mutableMapOf<String, CoupleDto>()

    /** users/{uid}.coupleId */
    val coupleIds = mutableMapOf<String, String?>()

    /** 이미 있는 코드처럼 거절할 횟수 (코드 충돌) */
    var takenCodes: Int = 0

    /** true면 다음 쓰기(connect · updateCouple)가 실패한다 */
    var failNext: Boolean = false

    override suspend fun createPairCode(code: PairCodeDto): Boolean {
        val available = takenCodes == 0 && code.code !in pairCodes
        if (takenCodes > 0) takenCodes--
        if (available) pairCodes[code.code] = code
        return available
    }

    override suspend fun getPairCode(code: String): PairCodeDto? = pairCodes[code]

    override suspend fun findMyCouples(me: String): List<CoupleDto> = couples.values.filter { me in it.members }

    override suspend fun deletePairCode(code: String) {
        pairCodes.remove(code)
    }

    override suspend fun connect(
        code: String,
        me: String,
        couple: CoupleDto,
        restore: Boolean,
    ) {
        failIfRequested()
        pairCodes[code] = checkNotNull(pairCodes[code]).copy(usedBy = me)
        couples[couple.id] = couple
    }

    override suspend fun updateCouple(couple: CoupleDto) {
        failIfRequested()
        couples[couple.id] = couple
    }

    override suspend fun setMyCoupleId(
        me: String,
        coupleId: String?,
    ) {
        coupleIds[me] = coupleId
    }

    private fun failIfRequested() {
        if (failNext) {
            failNext = false
            error("서버 쓰기 실패 (테스트)")
        }
    }
}
