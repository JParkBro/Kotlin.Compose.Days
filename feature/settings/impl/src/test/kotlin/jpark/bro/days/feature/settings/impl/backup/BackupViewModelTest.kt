package jpark.bro.days.feature.settings.impl.backup

import app.cash.turbine.test
import jpark.bro.days.core.ads.RewardedAdResult
import jpark.bro.days.core.testing.FakeBackupRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/** product-spec 9장 시나리오 (오늘 = 2026.11.05) */
class BackupViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val seoul = ZoneId.of("Asia/Seoul")
    private val now = LocalDateTime.of(2026, 11, 5, 21, 0).atZone(seoul).toInstant()
    private val clock = Clock.fixed(now, seoul)
    private val repository = FakeBackupRepository(now = { now.toEpochMilli() })

    private fun viewModel() = BackupViewModel(repository, clock)

    private fun minutesAgo(minutes: Long): Long = now.minusSeconds(minutes * 60).toEpochMilli()

    @Test
    fun `마지막 백업 시각을 보여주고 없으면 비워 둠`() {
        assertNull(viewModel().state.value.lastBackupAt)

        repository.backedUpAt = minutesAgo(60)
        val state = viewModel().state.value
        assertFalse(state.isLoading)
        assertEquals(LocalDateTime.of(2026, 11, 5, 20, 0), state.lastBackupAt)
    }

    @Test
    fun `백업을 누르면 광고를 띄우고 끝까지 보면 백업함`() =
        runTest {
            val viewModel = viewModel()
            viewModel.event.test {
                viewModel.onAction(BackupAction.BackupClicked)
                assertEquals(BackupEvent.ShowRewardedAd, awaitItem())
                assertEquals(BackupWork.BACKUP, viewModel.state.value.work)

                viewModel.onAction(BackupAction.RewardedAdFinished(RewardedAdResult.Earned))
                assertEquals(BackupEvent.BackedUp, awaitItem())
            }
            assertEquals(1, repository.backupCount)
            assertNull(viewModel.state.value.work)
            assertEquals(LocalDateTime.of(2026, 11, 5, 21, 0), viewModel.state.value.lastBackupAt)
        }

    @Test
    fun `광고를 중간에 닫으면 백업하지 않음`() =
        runTest {
            val viewModel = viewModel()
            viewModel.event.test {
                viewModel.onAction(BackupAction.BackupClicked)
                awaitItem()
                viewModel.onAction(BackupAction.RewardedAdFinished(RewardedAdResult.Dismissed))
                expectNoEvents()
            }
            assertEquals(0, repository.backupCount)
            assertNull(viewModel.state.value.work)
        }

    @Test
    fun `광고를 불러오지 못하면 광고 없이 백업함`() =
        runTest {
            val viewModel = viewModel()
            viewModel.event.test {
                viewModel.onAction(BackupAction.BackupClicked)
                awaitItem()
                viewModel.onAction(BackupAction.RewardedAdFinished(RewardedAdResult.Failed))
                assertEquals(BackupEvent.BackedUp, awaitItem())
            }
            assertEquals(1, repository.backupCount)
        }

    @Test
    fun `10분 안에 다시 누르면 광고 없이 알림`() =
        runTest {
            repository.backedUpAt = minutesAgo(9)
            val viewModel = viewModel()
            viewModel.event.test {
                viewModel.onAction(BackupAction.BackupClicked)
                assertEquals(BackupEvent.BackupTooSoon, awaitItem())
            }
            assertNull(viewModel.state.value.work)
        }

    @Test
    fun `백업에 실패하면 알리고 다시 누를 수 있음`() =
        runTest {
            repository.failBackup = true
            val viewModel = viewModel()
            viewModel.event.test {
                viewModel.onAction(BackupAction.BackupClicked)
                awaitItem()
                viewModel.onAction(BackupAction.RewardedAdFinished(RewardedAdResult.Earned))
                assertEquals(BackupEvent.BackupFailed, awaitItem())
            }
            assertFalse(viewModel.state.value.isWorking)
        }

    @Test
    fun `복원은 확인 창을 거쳐 광고 없이 복원함`() =
        runTest {
            repository.backedUpAt = minutesAgo(60)
            repository.itemCount = 3
            val viewModel = viewModel()
            viewModel.onAction(BackupAction.RestoreClicked)
            assertTrue(viewModel.state.value.isRestoreConfirmVisible)

            viewModel.event.test {
                viewModel.onAction(BackupAction.RestoreConfirmed)
                assertEquals(BackupEvent.Restored(itemCount = 3), awaitItem())
            }
            assertEquals(1, repository.restoreCount)
            assertFalse(viewModel.state.value.isRestoreConfirmVisible)
            assertNull(viewModel.state.value.work)
        }

    @Test
    fun `복원 확인 창을 닫으면 복원하지 않음`() {
        val viewModel = viewModel()
        viewModel.onAction(BackupAction.RestoreClicked)
        viewModel.onAction(BackupAction.RestoreDismissed)

        assertFalse(viewModel.state.value.isRestoreConfirmVisible)
        assertEquals(0, repository.restoreCount)
    }

    @Test
    fun `백업이 없으면 복원할 것이 없다고 알림`() =
        runTest {
            val viewModel = viewModel()
            viewModel.event.test {
                viewModel.onAction(BackupAction.RestoreClicked)
                viewModel.onAction(BackupAction.RestoreConfirmed)
                assertEquals(BackupEvent.NoBackup, awaitItem())
            }
        }

    @Test
    fun `복원에 실패하면 알림`() =
        runTest {
            repository.backedUpAt = minutesAgo(60)
            repository.failRestore = true
            val viewModel = viewModel()
            viewModel.event.test {
                viewModel.onAction(BackupAction.RestoreClicked)
                viewModel.onAction(BackupAction.RestoreConfirmed)
                assertEquals(BackupEvent.RestoreFailed, awaitItem())
            }
        }
}
