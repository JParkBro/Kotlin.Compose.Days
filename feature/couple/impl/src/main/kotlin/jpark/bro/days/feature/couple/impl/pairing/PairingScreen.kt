package jpark.bro.days.feature.couple.impl.pairing

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysInfoBox
import jpark.bro.days.core.designsystem.component.DaysInfoBoxTone
import jpark.bro.days.core.designsystem.component.DaysPrimaryButton
import jpark.bro.days.core.designsystem.component.DaysSegment
import jpark.bro.days.core.designsystem.component.DaysSegmentedControl
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.model.PairCode
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.couple.impl.PairCodeConfirmKey
import jpark.bro.days.feature.couple.impl.R
import jpark.bro.days.feature.couple.impl.components.PairCodeInput
import jpark.bro.days.feature.settings.api.ProfileEditKey
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun PairingRoot(
    onNavigate: (NavKey) -> Unit,
    onConnected: () -> Unit,
    onBack: () -> Unit,
    viewModel: PairingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toastHostState = remember { SnackbarHostState() }
    val copied = stringResource(R.string.couple_pairing_copied)
    val checkFailed = stringResource(R.string.couple_pairing_check_failed)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is PairingEvent.CopyCode -> {
                context.copyToClipboard(event.code)
                toastHostState.showSnackbar(copied)
            }

            PairingEvent.OpenProfileEdit -> {
                onNavigate(ProfileEditKey)
            }

            is PairingEvent.NavigateToConfirm -> {
                onNavigate(PairCodeConfirmKey(event.code))
            }

            PairingEvent.CheckFailed -> {
                toastHostState.showSnackbar(checkFailed)
            }

            PairingEvent.Connected -> {
                onConnected()
            }

            PairingEvent.NavigateBack -> {
                onBack()
            }
        }
    }

    // 남은 시간 표시용 (기기 시각, 1초마다). 서버를 부르지 않는다
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(TICK_MILLIS)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PairingScreen(state = state, now = now, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 커플 연결: 내 코드 · 코드 입력 탭 (product-spec 7장, 시안 couple-my-code · couple-code-input) */
@Composable
private fun PairingScreen(
    state: PairingState,
    now: Long,
    onAction: (PairingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .imePadding(),
    ) {
        DaysTopBar(
            title = stringResource(R.string.couple_pairing_title),
            isScrolled = scrollState.canScrollBackward,
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.couple_back),
                    onClick = { onAction(PairingAction.BackClicked) },
                )
            },
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            DaysSegmentedControl(
                segments =
                    listOf(
                        DaysSegment(stringResource(R.string.couple_pairing_tab_my_code)),
                        DaysSegment(stringResource(R.string.couple_pairing_tab_input)),
                    ),
                selectedIndex = state.tab.ordinal,
                onSelect = { onAction(PairingAction.TabSelected(PairingTab.entries[it])) },
                modifier = Modifier.testTag("pairing_tabs"),
            )
            when (state.tab) {
                PairingTab.MY_CODE -> MyCodeTab(state = state, now = now, onAction = onAction)
                PairingTab.INPUT -> InputTab(state = state, onAction = onAction)
            }
        }
    }
}

private fun Context.copyToClipboard(code: String) {
    val clipboard = getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText(code, code))
}

private const val TICK_MILLIS = 1_000L

private const val PREVIEW_NOW = 1_793_836_800_000L
private const val PREVIEW_REMAINING = 85_265_000L

@ScreenPreviews
@Composable
private fun PairingMyCodePreview() {
    AppTheme {
        PairingScreen(
            state = PairingState(isIssuing = false, myCode = PairCode("K7Q2MX", "지민", PREVIEW_NOW + PREVIEW_REMAINING)),
            now = PREVIEW_NOW,
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun PairingInputPreview() {
    AppTheme {
        PairingScreen(
            state =
                PairingState(
                    tab = PairingTab.INPUT,
                    isIssuing = false,
                    cells = listOf('K', '7', 'Q', null, null, null),
                    activeCell = 3,
                ),
            now = PREVIEW_NOW,
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun PairingProfileRequiredPreview() {
    AppTheme {
        PairingScreen(
            state = PairingState(isIssuing = false, issueError = IssueError.PROFILE_REQUIRED),
            now = PREVIEW_NOW,
            onAction = {},
        )
    }
}
