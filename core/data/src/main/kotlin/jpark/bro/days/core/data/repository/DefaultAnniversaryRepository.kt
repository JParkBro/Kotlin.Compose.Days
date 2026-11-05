package jpark.bro.days.core.data.repository

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.session.UserSession
import jpark.bro.days.core.database.dao.AnniversaryHideDao
import jpark.bro.days.core.database.dao.CoupleDao
import jpark.bro.days.core.database.entity.AnniversaryHideEntity
import jpark.bro.days.core.database.entity.SyncState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultAnniversaryRepository(
    private val hideDao: AnniversaryHideDao,
    private val coupleDao: CoupleDao,
    private val userSession: UserSession,
    private val clock: Clock,
) : AnniversaryRepository {
    override fun observeHiddenRuleKeys(): Flow<Set<String>> =
        userSession
            .observeUserId()
            .flatMapLatest { userId ->
                if (userId == null) flowOf(null) else coupleDao.observeActiveCouple(userId).map { it?.id }
            }.distinctUntilChanged()
            .flatMapLatest { coupleId ->
                if (coupleId == null) flowOf(emptySet()) else hideDao.observeHiddenRuleKeys(coupleId).map { it.toSet() }
            }

    override suspend fun setHidden(
        ruleKey: String,
        hidden: Boolean,
    ): Result<Unit> =
        suspendRunCatching {
            val me = userSession.currentUserId()
            val coupleId = requireNotNull(coupleDao.getActiveCouple(me)?.id) { "연결된 커플이 없습니다." }
            val existing = hideDao.getHide(coupleId, ruleKey)
            val isHidden = existing != null && existing.deletedAt == null
            if (isHidden == hidden) return@suspendRunCatching
            val now = clock.millis()
            // 숨김 해제도 삭제하지 않고 deleted_at을 남겨 상대 기기에 전달한다 (sync-and-pairing.md 보안 규칙)
            hideDao.upsertHide(
                AnniversaryHideEntity(
                    coupleId = coupleId,
                    ruleKey = ruleKey,
                    createdAt = if (hidden) now else existing?.createdAt ?: now,
                    deletedAt = if (hidden) null else now,
                    serverUpdatedAt = existing?.serverUpdatedAt,
                    syncState = SyncState.PENDING.name,
                ),
            )
        }
}
