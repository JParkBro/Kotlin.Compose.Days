package jpark.bro.days.core.data.repository

import jpark.bro.days.core.common.IdGenerator
import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.couple.ACTIVE
import jpark.bro.days.core.data.couple.CoupleTransitions
import jpark.bro.days.core.data.couple.DISCONNECTED
import jpark.bro.days.core.data.couple.PAIR_CODE_VALID_MILLIS
import jpark.bro.days.core.data.couple.PairingException
import jpark.bro.days.core.data.couple.ProfileRequiredException
import jpark.bro.days.core.data.couple.RECONNECT_WINDOW_MILLIS
import jpark.bro.days.core.data.couple.newPairCode
import jpark.bro.days.core.data.couple.normalizePairCode
import jpark.bro.days.core.data.couple.toDto
import jpark.bro.days.core.data.couple.toEntity
import jpark.bro.days.core.data.couple.toModel
import jpark.bro.days.core.data.session.UserSession
import jpark.bro.days.core.database.dao.UserDao
import jpark.bro.days.core.datastore.IssuedPairCode
import jpark.bro.days.core.firebase.sync.CoupleDto
import jpark.bro.days.core.firebase.sync.CoupleRemoteDataSource
import jpark.bro.days.core.firebase.sync.PairCodeDto
import jpark.bro.days.core.model.Couple
import jpark.bro.days.core.model.PairCode
import jpark.bro.days.core.model.PairCodeCheck
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultCoupleRepository(
    private val transitions: CoupleTransitions,
    private val remote: CoupleRemoteDataSource,
    private val userDao: UserDao,
    private val userSession: UserSession,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) : CoupleRepository {
    override fun observeCouple(): Flow<Couple?> =
        userSession.observeUserId().flatMapLatest { userId ->
            if (userId == null) flowOf(null) else transitions.observeActiveCouple(userId).map { it?.toModel(userId) }
        }

    override suspend fun issueCode(): Result<PairCode> =
        suspendRunCatching {
            val me = userSession.currentUserId()
            if (transitions.getActiveCouple(me) != null) throw PairingException(PairCodeCheck.AlreadyConnected)
            val nickname = userDao.getUser(me)?.nickname ?: throw ProfileRequiredException()
            val now = clock.millis()
            val reusable = transitions.issuedCode(me)?.takeIf { it.expiresAt - now > REISSUE_MARGIN_MILLIS }
            if (reusable != null) return@suspendRunCatching PairCode(reusable.code, nickname, reusable.expiresAt)

            transitions.forgetIssuedCode(me)
            val expiresAt = now + PAIR_CODE_VALID_MILLIS
            val code =
                generateSequence { newPairCode(Random.Default) }
                    .take(MAX_CODE_ATTEMPTS)
                    .firstOrNull { remote.createPairCode(PairCodeDto(it, me, nickname, expiresAt, now)) }
                    ?: error("코드를 만들지 못했습니다.")
            transitions.rememberIssuedCode(IssuedPairCode(me, code, expiresAt))
            PairCode(code, nickname, expiresAt)
        }

    override suspend fun checkCode(input: String): Result<PairCodeCheck> =
        suspendRunCatching { inspect(userSession.currentUserId(), input).first }

    override suspend fun connect(
        input: String,
        startDate: LocalDate,
        countFirstDayAsOne: Boolean,
    ): Result<Couple> =
        suspendRunCatching {
            val me = userSession.currentUserId()
            val (check, pairCode) = inspect(me, input)
            if (check !is PairCodeCheck.Valid || pairCode == null) throw PairingException(check)
            val now = clock.millis()
            val restorable =
                remote.findMyCouples(me).firstOrNull {
                    it.status == DISCONNECTED &&
                        it.members.toSet() == setOf(pairCode.creatorUid, me) &&
                        now - (it.disconnectedAt ?: 0) <= RECONNECT_WINDOW_MILLIS
                }
            val couple =
                (
                    restorable
                        ?: CoupleDto(
                            idGenerator.newId(),
                            listOf(pairCode.creatorUid, me),
                            "",
                            false,
                            ACTIVE,
                            "",
                            null,
                            now,
                            now,
                        )
                ).copy(
                    startDate = startDate.toString(),
                    countFirstDayAsOne = countFirstDayAsOne,
                    status = ACTIVE,
                    pairCode = pairCode.code,
                    disconnectedAt = null,
                    updatedAt = now,
                )
            remote.connect(pairCode.code, me, couple, restore = restorable != null)
            transitions.apply(me, listOf(couple))
            couple.toEntity().toModel(me)
        }

    override suspend fun updateCouple(
        startDate: LocalDate,
        countFirstDayAsOne: Boolean,
    ): Result<Unit> =
        suspendRunCatching {
            require(!startDate.isAfter(LocalDate.now(clock))) { "사귄 날이 오늘 이후입니다." }
            val me = userSession.currentUserId()
            val couple = checkNotNull(transitions.getActiveCouple(me)) { "연결된 커플이 없습니다." }
            val updated =
                couple.toDto().copy(
                    startDate = startDate.toString(),
                    countFirstDayAsOne = countFirstDayAsOne,
                    updatedAt = clock.millis(),
                )
            remote.updateCouple(updated)
            transitions.apply(me, listOf(updated))
        }

    override suspend fun disconnect(deleteCopies: Boolean): Result<Unit> =
        suspendRunCatching {
            val me = userSession.currentUserId()
            val couple = checkNotNull(transitions.getActiveCouple(me)) { "연결된 커플이 없습니다." }
            val now = clock.millis()
            val disconnected = couple.toDto().copy(status = DISCONNECTED, disconnectedAt = now, updatedAt = now)
            // 리스너가 먼저 해제를 받아도 사본을 만들지 않도록 서버에 쓰기 전에 표시한다
            if (deleteCopies) transitions.skipCopiesOnce(couple.id)
            remote.updateCouple(disconnected)
            transitions.apply(me, listOf(disconnected))
        }

    /** 코드 확인 결과와 (조회했으면) 코드 문서 */
    private suspend fun inspect(
        me: String,
        input: String,
    ): Pair<PairCodeCheck, PairCodeDto?> {
        val code = normalizePairCode(input)
        val pairCode =
            if (code == null || transitions.getActiveCouple(me) != null) null else remote.getPairCode(code)
        val check =
            when {
                code == null -> PairCodeCheck.NotFound
                transitions.getActiveCouple(me) != null -> PairCodeCheck.AlreadyConnected
                pairCode == null -> PairCodeCheck.NotFound
                pairCode.creatorUid == me -> PairCodeCheck.OwnCode
                pairCode.usedBy != null -> PairCodeCheck.AlreadyUsed
                pairCode.expiresAt <= clock.millis() -> PairCodeCheck.Expired
                else -> PairCodeCheck.Valid(PairCode(pairCode.code, pairCode.creatorNickname, pairCode.expiresAt))
            }
        return check to pairCode
    }

    private companion object {
        const val MAX_CODE_ATTEMPTS = 5

        /** 남은 시간이 이보다 짧으면 새 코드를 발급한다 */
        const val REISSUE_MARGIN_MILLIS = 10 * 60 * 1000L
    }
}
