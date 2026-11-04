package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.CoupleRepository
import jpark.bro.days.core.model.Couple
import jpark.bro.days.core.model.PairCode
import jpark.bro.days.core.model.PairCodeCheck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

/** 결과를 정해 두는 Fake. [couple]을 바꾸면 연결 · 해제를 흉내 낸다 */
class FakeCoupleRepository(
    initial: Couple? = null,
) : CoupleRepository {
    val couple = MutableStateFlow(initial)

    var issueResult: Result<PairCode> = Result.success(PairCode("K7Q2MX", "지민", Long.MAX_VALUE))
    var checkResult: Result<PairCodeCheck> =
        Result.success(
            PairCodeCheck.Valid(PairCode("K7Q2MX", "지민", Long.MAX_VALUE)),
        )

    /** null이면 성공: [couple]을 연결된 커플로 바꾼다 */
    var connectFailure: Throwable? = null
    var updateFailure: Throwable? = null
    var disconnectFailure: Throwable? = null

    val checkedInputs = mutableListOf<String>()
    var lastDeleteCopies: Boolean? = null

    override fun observeCouple(): Flow<Couple?> = couple

    override suspend fun issueCode(): Result<PairCode> = issueResult

    override suspend fun checkCode(input: String): Result<PairCodeCheck> {
        checkedInputs += input
        return checkResult
    }

    override suspend fun connect(
        input: String,
        startDate: LocalDate,
        countFirstDayAsOne: Boolean,
    ): Result<Couple> {
        connectFailure?.let { return Result.failure(it) }
        val connected = Couple("couple-1", "user-partner", startDate, countFirstDayAsOne)
        couple.value = connected
        return Result.success(connected)
    }

    override suspend fun updateCouple(
        startDate: LocalDate,
        countFirstDayAsOne: Boolean,
    ): Result<Unit> {
        updateFailure?.let { return Result.failure(it) }
        couple.value = couple.value?.copy(startDate = startDate, countFirstDayAsOne = countFirstDayAsOne)
        return Result.success(Unit)
    }

    override suspend fun disconnect(deleteCopies: Boolean): Result<Unit> {
        disconnectFailure?.let { return Result.failure(it) }
        lastDeleteCopies = deleteCopies
        couple.value = null
        return Result.success(Unit)
    }
}
