package jpark.bro.days.core.sync

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.couple.CoupleTransitions
import jpark.bro.days.core.firebase.sync.SyncRemoteDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * 동기화 한 단위씩 (sync-and-pairing.md 2장). 언제 실행할지는 [SyncManager] · Worker가 정한다.
 * - Push: 사진 → 내 프로필 → 공유 항목 · 숨긴 기념일 순 (사진을 먼저 올려야 그 사진을 가리키는 문서를 올린다)
 * - Pull: 커플 상태 → 상대 프로필 → 공유 항목 · 숨긴 기념일
 * 오류는 밖으로 던지지 않고 결과로 돌려준다. 실패한 행은 sync_state = FAILED로 남는다.
 * (공통 로거가 생기면 실패 종류만 기록한다. android.util.Log 직접 사용 금지, 개인정보 금지)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SyncEngine internal constructor(
    private val remote: SyncRemoteDataSource,
    private val couples: CoupleTransitions,
    private val coupleData: CoupleDataSync,
    private val profileSync: ProfileSync,
    private val imageSync: ImageSync,
) {
    /** 올릴 것이 있으면 0보다 크다 */
    fun observeUnsyncedCount(userId: String): Flow<Int> =
        combine(
            coupleData.observeUnsyncedCount(userId),
            profileSync.observeUnsyncedCount(userId),
            imageSync.observeUploadCount(userId),
        ) { data, profile, images -> data + profile + images }

    fun observeActiveCoupleId(userId: String): Flow<String?> =
        couples.observeActiveCouple(userId).map { it?.id }.distinctUntilChanged()

    /** 내 변경을 올린다. 하나라도 실패하면 false (Worker가 백오프로 다시 시도) */
    suspend fun push(userId: String): Boolean {
        val images = imageSync.push(userId)
        val profile = profileSync.push(userId)
        val data = profile && coupleData.push(userId)
        return images && profile && data
    }

    /** 백그라운드 1회성 조회. 로컬에 연결된 커플이 있을 때만 부른다 (Worker) */
    suspend fun pullOnce(userId: String): Boolean =
        suspendRunCatching {
            profileSync.pullMineIfMissing(userId)
            couples.apply(userId, remote.getCouples(userId))
            val couple = couples.getActiveCouple(userId) ?: return@suspendRunCatching
            profileSync.pullPartner(couple.partnerOf(userId))
            coupleData.pull(couple.id)
        }.isSuccess

    /**
     * 포그라운드 리스너. 취소될 때까지 돌아간다: 내 커플 문서 + 연결된 커플의 공유 항목 · 숨긴 기념일 변경분 + 상대 프로필.
     * 리스너 하나가 오류(권한 거부 등)로 끝나도 나머지는 계속 돈다. 다음 포그라운드 진입 때 다시 붙는다.
     */
    suspend fun listen(userId: String): Unit =
        coroutineScope {
            launch { profileSync.pullMineIfMissing(userId) }
            launch {
                remote
                    .observeCouples(userId)
                    .catch { /* 다음 포그라운드 진입 때 다시 붙는다 */ }
                    .collect { couples.apply(userId, it) }
            }
            couples
                .observeActiveCouple(userId)
                .map { it?.let { couple -> couple.id to couple.partnerOf(userId) } }
                .distinctUntilChanged()
                .collectLatest { active ->
                    val (coupleId, partnerId) = active ?: return@collectLatest
                    coroutineScope {
                        launch { profileSync.listenPartner(partnerId) }
                        launch { coupleData.listen(coupleId) }
                    }
                }
        }
}
