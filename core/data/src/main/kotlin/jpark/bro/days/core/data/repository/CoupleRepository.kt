package jpark.bro.days.core.data.repository

import jpark.bro.days.core.model.Couple
import jpark.bro.days.core.model.PairCode
import jpark.bro.days.core.model.PairCodeCheck
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 커플 연결 · 관리 (product-spec 7장, sync-and-pairing.md 3 · 5장). 연결 · 관리는 온라인일 때만 된다.
 * 실패 이유: 코드 발급은 ProfileRequiredException(이름 없음), 연결은 PairingException(reason)
 */
interface CoupleRepository {
    /** 연결된 커플. 솔로 · 로그아웃이면 null */
    fun observeCouple(): Flow<Couple?>

    /** 내 코드. 24시간 안에 발급한 코드가 있으면 그대로 */
    suspend fun issueCode(): Result<PairCode>

    /** 입력한 코드 확인 (대소문자 무시) */
    suspend fun checkCode(input: String): Result<PairCodeCheck>

    /** 같은 상대와 해제 후 30일 안이면 기존 커플을 되살린다 */
    suspend fun connect(
        input: String,
        startDate: LocalDate,
        countFirstDayAsOne: Boolean,
    ): Result<Couple>

    suspend fun updateCouple(
        startDate: LocalDate,
        countFirstDayAsOne: Boolean,
    ): Result<Unit>

    /** [deleteCopies]면 공유 항목의 나만 보기 사본을 남기지 않는다 */
    suspend fun disconnect(deleteCopies: Boolean): Result<Unit>
}
