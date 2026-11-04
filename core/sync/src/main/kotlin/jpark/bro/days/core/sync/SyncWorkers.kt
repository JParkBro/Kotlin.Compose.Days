package jpark.bro.days.core.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import jpark.bro.days.core.firebase.auth.FirebaseAuthDataSource
import jpark.bro.days.core.firebase.config.RemoteFlags
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/**
 * 내 변경 올리기. 연속 저장을 묶도록 [PUSH_DELAY_SECONDS] 뒤에, 네트워크가 있을 때 한 번 실행한다.
 * 실패하면 백오프로 [MAX_PUSH_ATTEMPTS]번까지 다시 시도하고, 그 뒤에는 다음 저장 · 백그라운드 Pull 때 다시 올린다.
 * Worker는 Android 진입점이라 Koin에서 직접 꺼낸다.
 */
internal class SyncPushWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params),
    KoinComponent {
    private val engine: SyncEngine by inject()
    private val authDataSource: FirebaseAuthDataSource by inject()
    private val remoteFlags: RemoteFlags by inject()

    override suspend fun doWork(): Result {
        val userId = authDataSource.currentUser?.uid
        return when {
            // 로그아웃 · 동기화 꺼짐: 다음 로그인 · 저장 때 다시 예약된다
            userId == null || !remoteFlags.syncEnabled.value -> Result.success()

            engine.push(userId) -> Result.success()

            runAttemptCount + 1 >= MAX_PUSH_ATTEMPTS -> Result.failure()

            else -> Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "sync_push"
        private const val PUSH_DELAY_SECONDS = 5L
        private const val BACKOFF_SECONDS = 30L
        private const val MAX_PUSH_ATTEMPTS = 5

        /** 이미 예약 · 실행 중이면 새로 만들지 않는다 (실행 중 새 저장은 Push가 반복해서 함께 올린다) */
        fun enqueue(context: Context) {
            val request =
                OneTimeWorkRequestBuilder<SyncPushWorker>()
                    .setInitialDelay(PUSH_DELAY_SECONDS, TimeUnit.SECONDS)
                    .setConstraints(networkConnected())
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }
    }
}

/**
 * 백그라운드에서 상대 변경 받기 (리스너는 포그라운드에서만). 하루 4번 1회성 조회 후 남은 변경을 올린다.
 * 받은 항목은 Room에 쓰이고, 위젯은 Room을 구독하는 WidgetUpdater가 다시 그린다.
 */
internal class SyncPullWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params),
    KoinComponent {
    private val engine: SyncEngine by inject()
    private val authDataSource: FirebaseAuthDataSource by inject()
    private val remoteFlags: RemoteFlags by inject()

    override suspend fun doWork(): Result {
        val userId = authDataSource.currentUser?.uid
        if (userId != null) {
            remoteFlags.refresh()
            if (remoteFlags.syncEnabled.value) {
                engine.pullOnce(userId)
                engine.push(userId)
            }
        }
        // 주기 작업이라 실패해도 다음 주기에 다시 받는다
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "sync_pull_periodic"
        private const val INTERVAL_HOURS = 6L

        fun schedule(context: Context) {
            val request =
                PeriodicWorkRequestBuilder<SyncPullWorker>(INTERVAL_HOURS, TimeUnit.HOURS)
                    .setConstraints(networkConnected())
                    .build()
            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

private fun networkConnected(): Constraints =
    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
