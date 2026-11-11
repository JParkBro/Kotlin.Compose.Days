package jpark.bro.days.feature.settings.impl.withdraw

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.data.auth.GoogleSignInCancelledException
import jpark.bro.days.core.data.auth.GoogleSignInClient
import jpark.bro.days.core.data.auth.KakaoSignInCancelledException
import jpark.bro.days.core.data.auth.KakaoSignInClient
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysCheckbox
import jpark.bro.days.core.designsystem.component.DaysDangerButton
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.model.AuthProvider
import jpark.bro.days.core.model.Reauthentication
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.settings.impl.R
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun WithdrawRoot(
    onSessionEnded: () -> Unit,
    onBack: () -> Unit,
    viewModel: WithdrawViewModel = koinViewModel(),
    googleSignIn: GoogleSignInClient = koinInject(),
    kakaoSignIn: KakaoSignInClient = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toastHostState = remember { SnackbarHostState() }
    val mismatch = stringResource(R.string.settings_withdraw_mismatch)
    val failed = stringResource(R.string.settings_withdraw_failed)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is WithdrawEvent.RequestReauthentication -> {
                val result =
                    when (event.provider) {
                        AuthProvider.GOOGLE -> googleSignIn.requestIdToken(context).map(Reauthentication::Google)
                        AuthProvider.KAKAO -> kakaoSignIn.requestAccessToken(context).map(Reauthentication::Kakao)
                    }
                result
                    .onSuccess { viewModel.onAction(WithdrawAction.Reauthenticated(it)) }
                    .onFailure { error ->
                        val cancelled =
                            error is GoogleSignInCancelledException || error is KakaoSignInCancelledException
                        viewModel.onAction(WithdrawAction.ReauthenticationFailed(cancelled))
                    }
            }

            WithdrawEvent.Withdrawn -> {
                onSessionEnded()
            }

            WithdrawEvent.AccountMismatch -> {
                toastHostState.showSnackbar(mismatch)
            }

            WithdrawEvent.WithdrawFailed -> {
                toastHostState.showSnackbar(failed)
            }

            WithdrawEvent.NavigateBack -> {
                onBack()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WithdrawScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 회원 탈퇴 안내 · 확인 (product-spec 6장). 시안 없음 */
@Composable
private fun WithdrawScreen(
    state: WithdrawState,
    onAction: (WithdrawAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.bg),
    ) {
        DaysTopBar(
            title = stringResource(R.string.settings_withdraw_title),
            isScrolled = scrollState.canScrollBackward,
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.settings_withdraw_back),
                    onClick = { onAction(WithdrawAction.BackClicked) },
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
            Text(
                text = stringResource(R.string.settings_withdraw_headline),
                style = typography.headlineMd,
                color = colors.textPrimary,
            )
            DaysCard {
                Column(
                    modifier = Modifier.padding(AppSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    state.notices().forEach { notice ->
                        Text(text = "• $notice", style = typography.bodyMd, color = colors.textPrimary)
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                DaysCheckbox(
                    checked = state.isConfirmed,
                    onCheckedChange = { onAction(WithdrawAction.ConfirmChanged(it)) },
                    enabled = !state.isWorking,
                    modifier = Modifier.testTag("withdraw_confirm"),
                )
                Text(
                    text = stringResource(R.string.settings_withdraw_confirm),
                    style = typography.bodyMd,
                    color = colors.textPrimary,
                )
            }
        }
        DaysDangerButton(
            text =
                stringResource(
                    if (state.isWorking) R.string.settings_withdraw_working else R.string.settings_withdraw_button,
                ),
            onClick = { onAction(WithdrawAction.WithdrawClicked) },
            enabled = state.canWithdraw,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(AppSpacing.marginMobile)
                    .testTag("withdraw_button"),
        )
    }
}

/** 탈퇴하면 생기는 일. 커플이면 연결 해제 · 사본 · 사진 안내를 더한다 */
@Composable
private fun WithdrawState.notices(): List<String> =
    buildList {
        add(stringResource(R.string.settings_withdraw_notice_account))
        add(stringResource(R.string.settings_withdraw_notice_device))
        if (isCoupled) {
            val partner = partnerName ?: stringResource(R.string.settings_withdraw_partner_fallback)
            add(stringResource(R.string.settings_withdraw_notice_couple, partner))
            add(stringResource(R.string.settings_withdraw_notice_photos))
        }
        add(stringResource(R.string.settings_withdraw_notice_support))
        val provider =
            when (provider) {
                AuthProvider.KAKAO -> stringResource(R.string.settings_account_provider_kakao)
                AuthProvider.GOOGLE, null -> stringResource(R.string.settings_account_provider_google)
            }
        add(stringResource(R.string.settings_withdraw_notice_reauth, provider))
    }

@ScreenPreviews
@Composable
private fun WithdrawScreenCouplePreview() {
    AppTheme {
        WithdrawScreen(
            state =
                WithdrawState(
                    provider = AuthProvider.KAKAO,
                    isCoupled = true,
                    partnerName = "지민",
                    isConfirmed = true,
                ),
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun WithdrawScreenSoloPreview() {
    AppTheme {
        WithdrawScreen(state = WithdrawState(provider = AuthProvider.GOOGLE), onAction = {})
    }
}
