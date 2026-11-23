package jpark.bro.days.update

import android.app.Application
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import jpark.bro.days.core.testing.FakeSettingsRepository
import jpark.bro.days.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

// DaysApplication(Firebase 초기화)을 띄우지 않는다
@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class UpdateViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val appUpdateManager = FakeAppUpdateManager(ApplicationProvider.getApplicationContext())
    private val settingsRepository = FakeSettingsRepository()
    private val viewModel by lazy { viewModelOn(TODAY) }

    /** 앱을 새로 연 것처럼 [date]에 ViewModel을 만든다 */
    private fun viewModelOn(date: LocalDate) =
        UpdateViewModel(appUpdateManager, settingsRepository, Clock.fixed(date.atStartOfDay(SEOUL).toInstant(), SEOUL))

    @Test
    fun `새 버전이 없으면 아무것도 묻지 않음`() {
        appUpdateManager.setUpdateNotAvailable()

        viewModel.onAction(UpdateAction.AppStarted)
        idle()

        assertEquals(UpdatePrompt.None, viewModel.state.value.prompt)
    }

    @Test
    fun `새 버전이 있으면 업데이트 안내`() {
        appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)

        viewModel.onAction(UpdateAction.AppStarted)
        idle()

        assertTrue(viewModel.state.value.prompt is UpdatePrompt.UpdateAvailable)
    }

    @Test
    fun `앱으로 돌아왔을 때는 새 버전을 다시 안내하지 않음`() {
        appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)

        viewModel.onAction(UpdateAction.AppResumed)
        idle()

        assertEquals(UpdatePrompt.None, viewModel.state.value.prompt)
    }

    @Test
    fun `업데이트 안내를 미루면 그날 다시 열어도 묻지 않음`() {
        appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)
        viewModel.onAction(UpdateAction.AppStarted)
        idle()
        viewModel.onAction(UpdateAction.UpdateDismissed)
        assertEquals(TODAY, settingsRepository.updatePromptDismissedDate)

        val reopened = viewModelOn(TODAY)
        reopened.onAction(UpdateAction.AppStarted)
        idle()

        assertEquals(UpdatePrompt.None, reopened.state.value.prompt)
    }

    @Test
    fun `업데이트 안내를 미룬 다음 날 열면 다시 물음`() {
        settingsRepository.updatePromptDismissedDate = TODAY
        appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)

        val nextDay = viewModelOn(TODAY.plusDays(1))
        nextDay.onAction(UpdateAction.AppStarted)
        idle()

        assertTrue(nextDay.state.value.prompt is UpdatePrompt.UpdateAvailable)
    }

    @Test
    fun `나중에를 누르면 안내를 닫고 업데이트하지 않음`() =
        runTest {
            appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)
            viewModel.onAction(UpdateAction.AppStarted)
            idle()

            viewModel.event.test {
                viewModel.onAction(UpdateAction.UpdateDismissed)
                expectNoEvents()
            }
            assertEquals(UpdatePrompt.None, viewModel.state.value.prompt)
        }

    @Test
    fun `업데이트를 누르면 Play 업데이트 화면을 열고 안내를 닫음`() =
        runTest {
            appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)
            viewModel.onAction(UpdateAction.AppStarted)
            idle()

            viewModel.event.test {
                viewModel.onAction(UpdateAction.UpdateConfirmed)
                assertTrue(awaitItem() is UpdateEvent.StartUpdateFlow)
            }
            assertEquals(UpdatePrompt.None, viewModel.state.value.prompt)
        }

    @Test
    fun `다운로드가 끝나면 재시작을 묻고 재시작하면 설치`() =
        runTest {
            appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)
            viewModel.onAction(UpdateAction.AppStarted)
            idle()
            startDownload()

            appUpdateManager.downloadCompletes()
            idle()
            assertEquals(UpdatePrompt.ReadyToInstall, viewModel.state.value.prompt)

            viewModel.event.test {
                viewModel.onAction(UpdateAction.InstallConfirmed)
                assertEquals(UpdateEvent.CompleteUpdate, awaitItem())
            }
            assertEquals(UpdatePrompt.None, viewModel.state.value.prompt)
        }

    @Test
    fun `재시작을 미루면 앱으로 돌아올 때 다시 물음`() {
        appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)
        viewModel.onAction(UpdateAction.AppStarted)
        idle()
        startDownload()
        appUpdateManager.downloadCompletes()
        idle()

        viewModel.onAction(UpdateAction.InstallDismissed)
        assertEquals(UpdatePrompt.None, viewModel.state.value.prompt)
        assertNull(settingsRepository.updatePromptDismissedDate)

        viewModel.onAction(UpdateAction.AppResumed)
        idle()
        assertEquals(UpdatePrompt.ReadyToInstall, viewModel.state.value.prompt)
    }

    @Test
    fun `다운로드가 끝난 채로 앱을 열면 업데이트 안내 대신 재시작을 물음`() {
        appUpdateManager.setUpdateAvailable(NEW_VERSION_CODE)
        startDownload()
        appUpdateManager.downloadCompletes()

        viewModel.onAction(UpdateAction.AppStarted)
        idle()

        assertEquals(UpdatePrompt.ReadyToInstall, viewModel.state.value.prompt)
    }

    /** Activity가 하는 Play 호출 → 사용자 동의 → 다운로드 시작 */
    private fun startDownload() {
        val info = appUpdateManager.appUpdateInfo.result
        appUpdateManager.startUpdateFlowForResult(
            info,
            NoOpLauncher,
            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
        )
        appUpdateManager.userAcceptsUpdate()
        appUpdateManager.downloadStarts()
        idle()
    }

    /** Play Task 콜백은 메인 Looper로 온다 */
    private fun idle() {
        shadowOf(Looper.getMainLooper()).idle()
    }

    private object NoOpLauncher : ActivityResultLauncher<IntentSenderRequest>() {
        override val contract: ActivityResultContract<IntentSenderRequest, *> =
            ActivityResultContracts.StartIntentSenderForResult()

        override fun launch(
            input: IntentSenderRequest,
            options: ActivityOptionsCompat?,
        ) = Unit

        override fun unregister() = Unit
    }

    private companion object {
        const val NEW_VERSION_CODE = 100
        val TODAY: LocalDate = LocalDate.of(2026, 11, 5)
        val SEOUL: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
