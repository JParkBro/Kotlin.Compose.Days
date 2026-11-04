package jpark.bro.days.core.sync

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import jpark.bro.days.core.firebase.auth.FirebaseAuthDataSource
import jpark.bro.days.core.firebase.config.RemoteFlags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * 동기화를 언제 돌릴지 정한다 (sync-and-pairing.md 2장, firebase-cost-strategy.md 4장).
 * - 리스너: 로그인 + 앱이 화면에 떠 있음 + sync_enabled일 때만. 하나라도 바뀌면 바로 해제
 * - Push: 올릴 것이 생기면 [SyncPushWorker] 예약 (5초 디바운스 · 네트워크 있을 때)
 * - 백그라운드 Pull: 연결된 커플이 있을 때만 [SyncPullWorker]를 6시간마다
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SyncManager internal constructor(
    private val context: Context,
    private val authDataSource: FirebaseAuthDataSource,
    private val remoteFlags: RemoteFlags,
    private val engine: SyncEngine,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** 앱 시작 시 한 번 (Application) */
    fun start() {
        val userIds = authDataSource.observeUser().map { it?.uid }.distinctUntilChanged()
        val foreground =
            ProcessLifecycleOwner
                .get()
                .lifecycle.currentStateFlow
                .map { it.isAtLeast(Lifecycle.State.STARTED) }
                .distinctUntilChanged()
        val enabled = remoteFlags.syncEnabled

        // 차단 스위치는 화면에 올라올 때마다 다시 받는다 (최소 간격은 FirebaseSetup)
        scope.launch { foreground.filter { it }.collect { remoteFlags.refresh() } }
        scope.launch {
            listenTarget(userIds, foreground, enabled).collectLatest { userId -> userId?.let { engine.listen(it) } }
        }
        scope.launch {
            backgroundTarget(userIds, enabled)
                .flatMapLatest { userId -> userId?.let(engine::observeUnsyncedCount) ?: flowOf(0) }
                .collect { count -> if (count > 0) SyncPushWorker.enqueue(context) }
        }
        scope.launch {
            backgroundTarget(userIds, enabled)
                .flatMapLatest { userId -> userId?.let(engine::observeActiveCoupleId) ?: flowOf(null) }
                .collect { coupleId ->
                    if (coupleId != null) SyncPullWorker.schedule(context) else SyncPullWorker.cancel(context)
                }
        }
    }
}

/** 리스너를 붙일 사용자. 로그아웃 · 백그라운드 · 동기화 꺼짐이면 null */
internal fun listenTarget(
    userIds: Flow<String?>,
    foreground: Flow<Boolean>,
    enabled: Flow<Boolean>,
): Flow<String?> =
    combine(userIds, foreground, enabled) { userId, isForeground, isEnabled ->
        userId.takeIf { isForeground && isEnabled }
    }.distinctUntilChanged()

/** Push · 백그라운드 Pull 대상 사용자. 로그아웃 · 동기화 꺼짐이면 null */
internal fun backgroundTarget(
    userIds: Flow<String?>,
    enabled: Flow<Boolean>,
): Flow<String?> = combine(userIds, enabled) { userId, isEnabled -> userId.takeIf { isEnabled } }.distinctUntilChanged()
