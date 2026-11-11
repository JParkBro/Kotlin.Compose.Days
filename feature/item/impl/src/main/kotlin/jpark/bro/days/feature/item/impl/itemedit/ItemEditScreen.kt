package jpark.bro.days.feature.item.impl.itemedit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.ads.rememberRewardedAdLauncher
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysDangerButton
import jpark.bro.days.core.designsystem.component.DaysFilterChip
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysPrimaryButton
import jpark.bro.days.core.designsystem.component.DaysSegment
import jpark.bro.days.core.designsystem.component.DaysSegmentedControl
import jpark.bro.days.core.designsystem.component.DaysTextField
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.Category
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import jpark.bro.days.core.model.Visibility
import jpark.bro.days.core.ui.DaysDatePickerDialog
import jpark.bro.days.core.ui.ImageLimitSheet
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.core.ui.label
import jpark.bro.days.core.ui.rememberNotificationPermissionRequest
import jpark.bro.days.core.ui.rememberPhotoPicker
import jpark.bro.days.core.ui.toDisplayText
import jpark.bro.days.feature.item.api.ItemEditKey
import jpark.bro.days.feature.item.impl.R
import jpark.bro.days.feature.item.impl.itemedit.components.ChipFlow
import jpark.bro.days.feature.item.impl.itemedit.components.CoverPhotoPicker
import jpark.bro.days.feature.item.impl.itemedit.components.DateRow
import jpark.bro.days.feature.item.impl.itemedit.components.DeleteConfirmDialog
import jpark.bro.days.feature.item.impl.itemedit.components.EditSection
import jpark.bro.days.feature.item.impl.itemedit.components.IconColorPicker
import jpark.bro.days.feature.item.impl.itemedit.components.ItemPreviewCard
import jpark.bro.days.feature.item.impl.itemedit.components.ItemTimePickerDialog
import jpark.bro.days.feature.item.impl.itemedit.components.SwitchRow
import jpark.bro.days.feature.item.impl.itemedit.components.VisibilityPicker
import jpark.bro.days.feature.item.impl.itemedit.components.label
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate
import java.time.LocalTime
import jpark.bro.days.core.designsystem.R as DesignR
import jpark.bro.days.core.ui.R as UiR

@Composable
internal fun ItemEditRoot(
    key: ItemEditKey,
    onBack: () -> Unit,
    viewModel: ItemEditViewModel = koinViewModel { parametersOf(key) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val saveFailed = stringResource(R.string.item_edit_save_failed)
    val deleteFailed = stringResource(R.string.item_edit_delete_failed)
    val photoFailed = stringResource(R.string.item_edit_photo_failed)
    val adFailed = stringResource(UiR.string.common_image_limit_ad_failed)
    val rewardPending = stringResource(UiR.string.common_image_limit_reward_pending)
    val openPhotoPicker = rememberPhotoPicker { viewModel.onAction(ItemEditAction.PhotoPicked(it)) }
    val showRewardedAd = rememberRewardedAdLauncher { viewModel.onAction(ItemEditAction.RewardedAdFinished(it)) }
    val requestNotificationPermission = rememberNotificationPermissionRequest()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            ItemEditEvent.NavigateBack -> onBack()
            ItemEditEvent.SaveFailed -> toastHostState.showSnackbar(saveFailed)
            ItemEditEvent.DeleteFailed -> toastHostState.showSnackbar(deleteFailed)
            ItemEditEvent.OpenPhotoPicker -> openPhotoPicker()
            ItemEditEvent.PhotoFailed -> toastHostState.showSnackbar(photoFailed)
            ItemEditEvent.RequestNotificationPermission -> requestNotificationPermission()
            is ItemEditEvent.ShowRewardedAd -> showRewardedAd(event.request)
            ItemEditEvent.RewardedAdFailed -> toastHostState.showSnackbar(adFailed)
            ItemEditEvent.RewardPending -> toastHostState.showSnackbar(rewardPending)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ItemEditScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 헤더: 취소 / 제목 / 저장 (product-spec 4-5) */
@Composable
private fun ItemEditScreen(
    state: ItemEditState,
    onAction: (ItemEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg),
    ) {
        DaysTopBar(
            title = stringResource(state.screenTitle),
            isScrolled = scrollState.canScrollBackward,
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_close,
                    contentDescription = stringResource(DesignR.string.common_cancel),
                    onClick = { onAction(ItemEditAction.CancelClicked) },
                )
            },
            actions = {
                DaysPrimaryButton(
                    text = stringResource(R.string.item_edit_save),
                    onClick = { onAction(ItemEditAction.SaveClicked) },
                    enabled = state.canSave,
                    size = DaysButtonSize.SMALL,
                    modifier = Modifier.testTag("item_edit_save"),
                )
            },
        )
        if (!state.isLoading) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = AppSpacing.marginMobile)
                        .padding(top = AppSpacing.sm, bottom = AppSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                ItemEditForm(state = state, onAction = onAction)
            }
        }
    }
    ItemEditDialogs(state = state, onAction = onAction)
}

@Composable
private fun ItemEditForm(
    state: ItemEditState,
    onAction: (ItemEditAction) -> Unit,
) {
    val isDday = state.type == ItemType.DDAY
    ItemPreviewCard(
        icon = state.icon,
        color = state.color,
        title = state.title,
        date = state.date,
        dday = state.previewDday,
    )
    EditSection(title = stringResource(if (isDday) R.string.item_edit_name_dday else R.string.item_edit_name_plan)) {
        DaysTextField(
            value = state.title,
            onValueChange = { onAction(ItemEditAction.TitleChanged(it)) },
            placeholder =
                stringResource(
                    if (isDday) R.string.item_edit_name_placeholder_dday else R.string.item_edit_name_placeholder_plan,
                ),
            modifier = Modifier.testTag("item_edit_title"),
        )
    }
    EditSection(title = stringResource(R.string.item_edit_icon_color)) {
        IconColorPicker(
            icon = state.icon,
            color = state.color,
            onIconSelect = { onAction(ItemEditAction.IconSelected(it)) },
            onColorSelect = { onAction(ItemEditAction.ColorSelected(it)) },
        )
    }
    if (state.canAddPhoto) {
        EditSection(title = stringResource(R.string.item_edit_photo)) {
            CoverPhotoPicker(
                file = state.backgroundFile,
                hasPhoto = state.backgroundImageId != null,
                isWorking = state.isPhotoWorking,
                remainingQuota = state.photoQuotaRemaining,
                onPick = { onAction(ItemEditAction.PhotoClicked) },
                onRemove = { onAction(ItemEditAction.PhotoRemoved) },
            )
        }
    }
    if (isDday) DdayFields(state = state, onAction = onAction) else PlanFields(state = state, onAction = onAction)
    EditSection(title = stringResource(R.string.item_edit_memo)) {
        DaysTextField(
            value = state.memo,
            onValueChange = { onAction(ItemEditAction.MemoChanged(it)) },
            placeholder = stringResource(R.string.item_edit_memo_placeholder),
            singleLine = false,
            modifier = Modifier.testTag("item_edit_memo"),
        )
    }
    EditSection(
        title = stringResource(R.string.item_edit_reminder),
        trailing = stringResource(R.string.item_edit_reminder_time),
    ) {
        ChipFlow {
            ReminderOffsets.forEach { offset ->
                DaysFilterChip(
                    text =
                        if (offset == 0) {
                            stringResource(R.string.item_edit_reminder_same_day)
                        } else {
                            stringResource(R.string.item_edit_reminder_days_before, offset)
                        },
                    selected = offset in state.reminderOffsets,
                    onClick = { onAction(ItemEditAction.ReminderToggled(offset)) },
                    role = Role.Checkbox,
                    modifier = Modifier.testTag("item_edit_reminder_$offset"),
                )
            }
        }
    }
    if (state.showVisibility) {
        EditSection(title = stringResource(R.string.item_edit_visibility)) {
            VisibilityPicker(
                visibility = state.visibility,
                isLocked = state.isVisibilityLocked,
                onSelect = { onAction(ItemEditAction.VisibilitySelected(it)) },
            )
        }
    }
    if (isDday) {
        SwitchRow(
            title = stringResource(R.string.item_edit_pin),
            description = stringResource(R.string.item_edit_pin_desc),
            checked = state.isPinned,
            onCheckedChange = { onAction(ItemEditAction.PinnedChanged(it)) },
            modifier = Modifier.testTag("item_edit_pin"),
        )
    }
    if (!state.isNew) {
        DaysDangerButton(
            text = stringResource(DesignR.string.common_delete),
            onClick = { onAction(ItemEditAction.DeleteClicked) },
            enabled = !state.isSaving,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("item_edit_delete"),
        )
    }
}

/** 카테고리 · 기준일 · 계산 방식 · 1일 세기 · 반복 */
@Composable
private fun DdayFields(
    state: ItemEditState,
    onAction: (ItemEditAction) -> Unit,
) {
    EditSection(title = stringResource(R.string.item_edit_category)) {
        ChipFlow {
            Category.entries.forEach { category ->
                DaysFilterChip(
                    text = stringResource(category.label),
                    selected = category == state.category,
                    onClick = { onAction(ItemEditAction.CategorySelected(category)) },
                )
            }
        }
    }
    EditSection(title = stringResource(R.string.item_edit_date)) {
        DateRow(
            label = stringResource(R.string.item_edit_date_base),
            date = state.date,
            onClick = { onAction(ItemEditAction.DateClicked) },
        )
    }
    EditSection(title = stringResource(R.string.item_edit_count_mode)) {
        DaysSegmentedControl(
            segments =
                listOf(
                    DaysSegment(stringResource(R.string.item_edit_count_mode_dday)),
                    DaysSegment(stringResource(R.string.item_edit_count_mode_dplus)),
                ),
            selectedIndex = CountMode.entries.indexOf(state.countMode),
            onSelect = { onAction(ItemEditAction.CountModeSelected(CountMode.entries[it])) },
        )
        // 1일 세기는 D+에서만 보인다 (product-spec 2-2)
        if (state.countMode == CountMode.DPLUS) {
            SwitchRow(
                title = stringResource(R.string.item_edit_count_first_day),
                description = stringResource(R.string.item_edit_count_first_day_desc),
                checked = state.countFirstDayAsOne,
                onCheckedChange = { onAction(ItemEditAction.CountFirstDayChanged(it)) },
                modifier = Modifier.testTag("item_edit_count_first_day"),
            )
        }
    }
    EditSection(title = stringResource(R.string.item_edit_repeat)) {
        DaysSegmentedControl(
            segments = RepeatRule.entries.map { DaysSegment(stringResource(it.label)) },
            selectedIndex = RepeatRule.entries.indexOf(state.repeatRule),
            onSelect = { onAction(ItemEditAction.RepeatSelected(RepeatRule.entries[it])) },
        )
    }
}

/** 날짜(선택) · 시간 · 장소 · 디데이로 세기 */
@Composable
private fun PlanFields(
    state: ItemEditState,
    onAction: (ItemEditAction) -> Unit,
) {
    EditSection(title = stringResource(R.string.item_edit_date)) {
        DateRow(
            label = stringResource(R.string.item_edit_date),
            date = state.date,
            onClick = { onAction(ItemEditAction.DateClicked) },
            onClear = { onAction(ItemEditAction.DateCleared) },
        )
    }
    EditSection(
        title = stringResource(R.string.item_edit_time),
        trailing = state.time?.takeIf { state.timeSlot == TimeSlot.EXACT }?.toDisplayText(),
    ) {
        ChipFlow {
            TimeSlot.entries.forEach { slot ->
                DaysFilterChip(
                    text = stringResource(slot.label),
                    selected = slot == state.timeSlot,
                    onClick = { onAction(ItemEditAction.TimeSlotSelected(slot)) },
                    modifier = Modifier.testTag("item_edit_time_${slot.name.lowercase()}"),
                )
            }
        }
    }
    EditSection(title = stringResource(R.string.item_edit_place)) {
        DaysTextField(
            value = state.place,
            onValueChange = { onAction(ItemEditAction.PlaceChanged(it)) },
            placeholder = stringResource(R.string.item_edit_place_placeholder),
            modifier = Modifier.testTag("item_edit_place"),
        )
    }
    SwitchRow(
        title = stringResource(R.string.item_edit_show_as_dday),
        description = stringResource(R.string.item_edit_show_as_dday_desc),
        checked = state.showAsDday,
        onCheckedChange = { onAction(ItemEditAction.ShowAsDdayChanged(it)) },
        modifier = Modifier.testTag("item_edit_show_as_dday"),
    )
}

@Composable
private fun ItemEditDialogs(
    state: ItemEditState,
    onAction: (ItemEditAction) -> Unit,
) {
    val dismiss = { onAction(ItemEditAction.DialogDismissed) }
    when (state.dialog) {
        ItemEditDialog.DATE -> {
            DaysDatePickerDialog(
                initial = state.date ?: state.today,
                onConfirm = { onAction(ItemEditAction.DateSelected(it)) },
                onDismiss = dismiss,
            )
        }

        ItemEditDialog.TIME -> {
            ItemTimePickerDialog(
                initial = state.time,
                onConfirm = { onAction(ItemEditAction.TimeSelected(it)) },
                onDismiss = dismiss,
            )
        }

        ItemEditDialog.DELETE_CONFIRM -> {
            DeleteConfirmDialog(onConfirm = { onAction(ItemEditAction.DeleteConfirmed) }, onDismiss = dismiss)
        }

        null -> {
            Unit
        }
    }
    state.imageLimit?.let { quota ->
        ImageLimitSheet(
            quota = quota,
            isWorking = state.isRewardWorking,
            onWatchAd = { onAction(ItemEditAction.WatchAdClicked) },
            onDismiss = { onAction(ItemEditAction.ImageLimitDismissed) },
        )
    }
}

private val ItemEditState.screenTitle: Int
    get() =
        when (type) {
            ItemType.DDAY -> if (isNew) R.string.item_edit_title_add_dday else R.string.item_edit_title_edit_dday
            ItemType.PLAN -> if (isNew) R.string.item_edit_title_add_plan else R.string.item_edit_title_edit_plan
        }

/** product-spec 9장 샘플 (오늘 = 2026.11.05) */
@Suppress("MagicNumber") // 샘플 데이터
private val PreviewToday: LocalDate = LocalDate.of(2026, 11, 5)

@ScreenPreviews
@Composable
private fun ItemEditNewDdayPreview() {
    AppTheme {
        ItemEditScreen(
            state = ItemEditState(type = ItemType.DDAY, today = PreviewToday, date = PreviewToday),
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun ItemEditDdayPreview() {
    AppTheme {
        ItemEditScreen(
            state =
                ItemEditState(
                    itemId = "discharge",
                    type = ItemType.DDAY,
                    today = PreviewToday,
                    title = "금연",
                    icon = ItemIcon.FLAG,
                    color = AppThemeType.SAGE,
                    category = Category.GOAL,
                    date = LocalDate.of(2026, 8, 7),
                    countMode = CountMode.DPLUS,
                    countFirstDayAsOne = true,
                    reminderOffsets = setOf(0, 7),
                    isPinned = true,
                    coupleId = "couple-1",
                ),
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun ItemEditPlanPreview() {
    AppTheme {
        ItemEditScreen(
            state =
                ItemEditState(
                    type = ItemType.PLAN,
                    today = PreviewToday,
                    title = "성수 저녁",
                    icon = ItemIcon.EVENT,
                    date = LocalDate.of(2026, 11, 6),
                    timeSlot = TimeSlot.EXACT,
                    time = LocalTime.of(19, 0),
                    place = "성수동 카페거리",
                    showAsDday = true,
                    reminderOffsets = setOf(1),
                    coupleId = "couple-1",
                    visibility = Visibility.SHARED,
                    isVisibilityLocked = true,
                ),
            onAction = {},
        )
    }
}
