package jpark.bro.days.core.model

import java.time.LocalDate

/** 연결된 커플 (product-spec 7장). 화면에는 연결된(ACTIVE) 커플만 보인다 */
data class Couple(
    val id: String,
    val partnerId: String,
    /** 사귄 날 */
    val startDate: LocalDate,
    /** 사귄 날을 1일로 세기 */
    val countFirstDayAsOne: Boolean,
)

/** 내가 발급한 커플 코드 (24시간 유효) */
data class PairCode(
    val code: String,
    val creatorNickname: String,
    /** epoch millis */
    val expiresAt: Long,
)

/** 입력한 코드를 확인한 결과 (product-spec 7장 · sync-and-pairing.md 3장) */
sealed interface PairCodeCheck {
    data class Valid(
        val code: PairCode,
    ) : PairCodeCheck

    data object NotFound : PairCodeCheck

    data object Expired : PairCodeCheck

    data object AlreadyUsed : PairCodeCheck

    /** 내가 발급한 코드 */
    data object OwnCode : PairCodeCheck

    /** 나는 이미 다른 커플에 연결되어 있음 */
    data object AlreadyConnected : PairCodeCheck
}
