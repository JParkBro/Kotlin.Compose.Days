package jpark.bro.days.feature.settings.impl.profileedit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.ads.rememberRewardedAdLauncher
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysInfoBox
import jpark.bro.days.core.designsystem.component.DaysInfoBoxTone
import jpark.bro.days.core.designsystem.component.DaysPrimaryButton
import jpark.bro.days.core.designsystem.component.DaysSecondaryButton
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.core.designsystem.component.DaysTextField
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.model.UserProfile
import jpark.bro.days.core.ui.DaysDatePickerDialog
import jpark.bro.days.core.ui.ImageLimitSheet
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.core.ui.ProfileAvatar
import jpark.bro.days.core.ui.rememberPhotoPicker
import jpark.bro.days.core.ui.toDisplayText
import jpark.bro.days.feature.settings.impl.R
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import jpark.bro.days.core.designsystem.R as DesignR
import jpark.bro.days.core.ui.R as UiR

@Composable
internal fun ProfileEditRoot(
    onBack: () -> Unit,
    viewModel: ProfileEditViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val saveFailed = stringResource(R.string.settings_profile_save_failed)
    val photoFailed = stringResource(R.string.settings_profile_photo_failed)
    val adFailed = stringResource(UiR.string.common_image_limit_ad_failed)
    val rewardPending = stringResource(UiR.string.common_image_limit_reward_pending)
    val openPhotoPicker = rememberPhotoPicker { viewModel.onAction(ProfileEditAction.PhotoPicked(it)) }
    val showRewardedAd = rememberRewardedAdLauncher { viewModel.onAction(ProfileEditAction.RewardedAdFinished(it)) }

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            ProfileEditEvent.Saved -> onBack()
            ProfileEditEvent.SaveFailed -> toastHostState.showSnackbar(saveFailed)
            ProfileEditEvent.NavigateBack -> onBack()
            ProfileEditEvent.OpenPhotoPicker -> openPhotoPicker()
            ProfileEditEvent.PhotoFailed -> toastHostState.showSnackbar(photoFailed)
            is ProfileEditEvent.ShowRewardedAd -> showRewardedAd(event.request)
            ProfileEditEvent.RewardedAdFailed -> toastHostState.showSnackbar(adFailed)
            ProfileEditEvent.RewardPending -> toastHostState.showSnackbar(rewardPending)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ProfileEditScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 프로필 수정 (product-spec 6장: 이름 · 사진 · 생일). 시안 없음 */
@Composable
private fun ProfileEditScreen(
    state: ProfileEditState,
    onAction: (ProfileEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.bg)
                .imePadding(),
    ) {
        DaysTopBar(
            title = stringResource(R.string.settings_profile_title),
            isScrolled = scrollState.canScrollBackward,
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.settings_profile_back),
                    onClick = { onAction(ProfileEditAction.BackClicked) },
                )
            },
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            PhotoSection(state = state, onAction = onAction)
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                FieldLabel(text = stringResource(R.string.settings_profile_nickname))
                DaysTextField(
                    value = state.nickname,
                    onValueChange = { onAction(ProfileEditAction.NicknameChanged(it)) },
                    placeholder = stringResource(R.string.settings_profile_nickname_placeholder),
                    enabled = !state.isLoading,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag("profile_nickname"),
                )
                Text(
                    text =
                        stringResource(
                            R.string.settings_profile_nickname_count,
                            state.nickname.length,
                            UserProfile.NICKNAME_MAX_LENGTH,
                        ),
                    style = typography.labelSm,
                    color = colors.textTertiary,
                    modifier = Modifier.align(Alignment.End),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                FieldLabel(text = stringResource(R.string.settings_profile_birth_date))
                BirthDateCard(birthDate = state.birthDate, onAction = onAction)
                DaysInfoBox(
                    text = stringResource(R.string.settings_profile_birth_date_hint),
                    tone = DaysInfoBoxTone.TIP,
                )
            }
        }
        DaysPrimaryButton(
            text = stringResource(R.string.settings_profile_save),
            onClick = { onAction(ProfileEditAction.SaveClicked) },
            enabled = state.canSave,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md)
                    .testTag("profile_save"),
        )
    }

    if (state.isDatePickerOpen) {
        DaysDatePickerDialog(
            initial = state.birthDate,
            maxDate = state.today,
            onConfirm = { onAction(ProfileEditAction.BirthDateSelected(it)) },
            onDismiss = { onAction(ProfileEditAction.BirthDateDismissed) },
        )
    }
    state.imageLimit?.let { quota ->
        ImageLimitSheet(
            quota = quota,
            isWorking = state.isRewardWorking,
            onWatchAd = { onAction(ProfileEditAction.WatchAdClicked) },
            onDismiss = { onAction(ProfileEditAction.ImageLimitDismissed) },
        )
    }
}

/** 프로필 사진: 원 + 변경 · 삭제. 솔로일 때는 이 기기에만 저장된다 */
@Composable
private fun PhotoSection(
    state: ProfileEditState,
    onAction: (ProfileEditAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        ProfileAvatar(
            file = state.photoFile,
            name = state.nickname.trim().ifEmpty { null },
            size = 96.dp,
            modifier =
                Modifier
                    .clickable(enabled = !state.isLoading && !state.isPhotoWorking) {
                        onAction(ProfileEditAction.PhotoClicked)
                    }.testTag("profile_photo"),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            DaysSoftButton(
                text = stringResource(R.string.settings_profile_photo_change),
                onClick = { onAction(ProfileEditAction.PhotoClicked) },
                enabled = !state.isLoading && !state.isPhotoWorking,
                size = DaysButtonSize.SMALL,
                modifier = Modifier.testTag("profile_photo_change"),
            )
            if (state.photoId != null) {
                DaysSecondaryButton(
                    text = stringResource(R.string.settings_profile_photo_remove),
                    onClick = { onAction(ProfileEditAction.PhotoRemoved) },
                    enabled = !state.isPhotoWorking,
                    size = DaysButtonSize.SMALL,
                    modifier = Modifier.testTag("profile_photo_remove"),
                )
            }
        }
        Text(
            text = stringResource(R.string.settings_profile_photo_hint),
            style = AppThemeTokens.typography.bodySm,
            color = AppThemeTokens.colors.textTertiary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, style = AppThemeTokens.typography.labelMd, color = AppThemeTokens.colors.textSecondary)
}

@Composable
private fun BirthDateCard(
    birthDate: LocalDate?,
    onAction: (ProfileEditAction) -> Unit,
) {
    val colors = AppThemeTokens.colors
    DaysCard {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = birthDate?.toDisplayText() ?: stringResource(R.string.settings_profile_birth_date_empty),
                style = AppThemeTokens.typography.bodyLg,
                color = if (birthDate != null) colors.textPrimary else colors.textTertiary,
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag("profile_birth_date"),
            )
            if (birthDate != null) {
                DaysSecondaryButton(
                    text = stringResource(R.string.settings_profile_birth_date_clear),
                    onClick = { onAction(ProfileEditAction.BirthDateCleared) },
                    size = DaysButtonSize.SMALL,
                    modifier = Modifier.testTag("profile_birth_date_clear"),
                )
            }
            DaysSoftButton(
                text = stringResource(R.string.settings_profile_birth_date_change),
                onClick = { onAction(ProfileEditAction.BirthDateClicked) },
                size = DaysButtonSize.SMALL,
                modifier = Modifier.testTag("profile_birth_date_change"),
            )
        }
    }
}

private val PreviewSaved = UserProfile(userId = "user-me", nickname = "현우", birthDate = LocalDate.parse("1998-04-12"))

@ScreenPreviews
@Composable
private fun ProfileEditFilledPreview() {
    AppTheme {
        ProfileEditScreen(
            state =
                ProfileEditState(
                    isLoading = false,
                    nickname = "현우",
                    birthDate = PreviewSaved.birthDate,
                    saved = PreviewSaved,
                ),
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun ProfileEditEmptyPreview() {
    AppTheme { ProfileEditScreen(state = ProfileEditState(isLoading = false), onAction = {}) }
}
