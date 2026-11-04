package jpark.bro.days.feature.settings.impl.profileedit

import androidx.compose.foundation.background
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.core.ui.toDisplayText
import jpark.bro.days.feature.settings.impl.R
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun ProfileEditRoot(
    onBack: () -> Unit,
    viewModel: ProfileEditViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val saveFailed = stringResource(R.string.settings_profile_save_failed)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            ProfileEditEvent.Saved -> onBack()
            ProfileEditEvent.SaveFailed -> toastHostState.showSnackbar(saveFailed)
            ProfileEditEvent.NavigateBack -> onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ProfileEditScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 프로필 수정 (product-spec 6장: 이름 · 생일. 사진은 이미지 저장소 단계에서). 시안 없음 */
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
