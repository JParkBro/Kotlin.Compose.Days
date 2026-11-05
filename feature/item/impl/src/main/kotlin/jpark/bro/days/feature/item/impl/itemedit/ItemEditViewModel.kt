package jpark.bro.days.feature.item.impl.itemedit

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.image.ImageDraft
import jpark.bro.days.core.data.repository.CoupleRepository
import jpark.bro.days.core.data.repository.ImageRepository
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.data.repository.observeImageFileOrNull
import jpark.bro.days.core.model.Category
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.ItemReminder
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import jpark.bro.days.core.model.Visibility
import jpark.bro.days.core.model.defaultIcon
import jpark.bro.days.core.model.itemColorFromKey
import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.item.api.ItemEditKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
internal class ItemEditViewModel(
    key: ItemEditKey,
    private val itemRepository: ItemRepository,
    coupleRepository: CoupleRepository,
    imageRepository: ImageRepository,
    clock: Clock,
) : MviViewModel<ItemEditState, ItemEditAction, ItemEditEvent>(initialState(key, LocalDate.now(clock))) {
    /** 수정 중인 원본. 화면에 없는 값(완료)을 저장할 때 유지한다 */
    private var original: Item? = null
    private var originalPinned: Boolean = false
    private val photo = ImageDraft(imageRepository, original = null)
    private val photoId = MutableStateFlow<String?>(null)

    init {
        photoId
            .flatMapLatest(imageRepository::observeImageFileOrNull)
            .onEach { file -> setState { copy(backgroundFile = file) } }
            .launchIn(viewModelScope)
        viewModelScope.launch {
            // 연결된 커플은 화면을 여는 시점 기준. 편집 중에 해제되면 공유 저장이 실패하고 안내한다
            val coupleId = coupleRepository.observeCouple().first()?.id
            val itemId = key.itemId
            if (itemId != null) {
                load(itemId, coupleId)
            } else {
                // 커플이면 새 항목은 우리 둘이가 기본 (시안 item-edit-dday)
                setState {
                    copy(coupleId = coupleId, visibility = if (coupleId != null) Visibility.SHARED else visibility)
                }
            }
        }
    }

    override fun onAction(action: ItemEditAction) {
        when (action) {
            is ItemEditAction.CommonInput -> setState { reduce(action) }
            is ItemEditAction.DdayInput -> setState { reduce(action) }
            is ItemEditAction.PlanInput -> setState { reduce(action) }
            ItemEditAction.DateClicked -> setState { copy(dialog = ItemEditDialog.DATE) }
            is ItemEditAction.DateSelected -> setState { copy(date = action.date, dialog = null) }
            ItemEditAction.DialogDismissed -> setState { copy(dialog = null) }
            ItemEditAction.SaveClicked -> save()
            ItemEditAction.CancelClicked -> cancel()
            ItemEditAction.PhotoClicked -> sendEvent(ItemEditEvent.OpenPhotoPicker)
            is ItemEditAction.PhotoPicked -> editPhoto { replace(action.uri).onFailure { photoFailed() } }
            ItemEditAction.PhotoRemoved -> editPhoto { remove() }
            ItemEditAction.DeleteClicked -> setState { copy(dialog = ItemEditDialog.DELETE_CONFIRM) }
            ItemEditAction.DeleteConfirmed -> delete()
        }
    }

    private suspend fun load(
        itemId: String,
        coupleId: String?,
    ) {
        val item = itemRepository.observeItem(itemId).first()
        if (item == null) {
            sendEvent(ItemEditEvent.NavigateBack)
            return
        }
        val reminders = itemRepository.observeReminders(itemId).first()
        val pinned = itemRepository.observePinnedItemId().first() == itemId
        original = item
        originalPinned = pinned
        photo.reset(item.content.backgroundImageId)
        photoId.value = photo.current
        setState { item.toState(today = today, reminders = reminders, pinned = pinned).copy(coupleId = coupleId) }
    }

    private fun editPhoto(block: suspend ImageDraft.() -> Unit) {
        if (currentState.isPhotoWorking) return
        setState { copy(isPhotoWorking = true) }
        viewModelScope.launch {
            photo.block()
            photoId.value = photo.current
            setState { copy(backgroundImageId = photo.current, isPhotoWorking = false) }
        }
    }

    private fun photoFailed() = sendEvent(ItemEditEvent.PhotoFailed)

    /** 저장하지 않고 닫으면 새로 고른 사진을 버린다 */
    private fun cancel() {
        viewModelScope.launch {
            photo.abandon()
            sendEvent(ItemEditEvent.NavigateBack)
        }
    }

    private fun save() {
        val state = currentState
        if (!state.canSave) return
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            val saved =
                itemRepository
                    .saveItem(id = state.itemId, content = state.toContent(original?.content))
                    .getOrElse { return@launch saveFailed() }
            val reminders = state.reminderOffsets.map { ItemReminder(offsetDays = it, timeOfDay = ReminderTime) }
            itemRepository.setReminders(saved.id, reminders).getOrElse { return@launch saveFailed() }
            // 대표 고정은 디데이만 (product-spec 4-5)
            if (state.type == ItemType.DDAY && state.isPinned != originalPinned) {
                itemRepository.setPinned(saved.id, state.isPinned).getOrElse { return@launch saveFailed() }
            }
            photo.commit()
            sendEvent(ItemEditEvent.NavigateBack)
        }
    }

    private fun saveFailed() {
        setState { copy(isSaving = false) }
        sendEvent(ItemEditEvent.SaveFailed)
    }

    private fun delete() {
        val itemId = currentState.itemId ?: return
        setState { copy(dialog = null, isSaving = true) }
        viewModelScope.launch {
            itemRepository
                .deleteItem(itemId)
                .onSuccess {
                    photo.abandon()
                    sendEvent(ItemEditEvent.NavigateBack)
                }.onFailure {
                    setState { copy(isSaving = false) }
                    sendEvent(ItemEditEvent.DeleteFailed)
                }
        }
    }
}

private fun ItemEditState.reduce(action: ItemEditAction.CommonInput): ItemEditState =
    when (action) {
        is ItemEditAction.TitleChanged -> {
            copy(title = action.title)
        }

        is ItemEditAction.IconSelected -> {
            copy(icon = action.icon, isIconChosen = true)
        }

        is ItemEditAction.ColorSelected -> {
            copy(color = action.color)
        }

        is ItemEditAction.MemoChanged -> {
            copy(memo = action.memo)
        }

        is ItemEditAction.VisibilitySelected -> {
            if (isVisibilityLocked) this else copy(visibility = action.visibility)
        }

        is ItemEditAction.ReminderToggled -> {
            val offset = action.offsetDays
            copy(
                reminderOffsets =
                    if (offset in
                        reminderOffsets
                    ) {
                        reminderOffsets - offset
                    } else {
                        reminderOffsets + offset
                    },
            )
        }
    }

private fun ItemEditState.reduce(action: ItemEditAction.DdayInput): ItemEditState =
    when (action) {
        is ItemEditAction.CategorySelected -> {
            copy(category = action.category, icon = if (isIconChosen) icon else action.category.defaultIcon)
        }

        // 1일 세기는 D+에서만 (product-spec 2-2)
        is ItemEditAction.CountModeSelected -> {
            copy(
                countMode = action.countMode,
                countFirstDayAsOne =
                    countFirstDayAsOne && action.countMode == CountMode.DPLUS,
            )
        }

        is ItemEditAction.CountFirstDayChanged -> {
            copy(countFirstDayAsOne = action.enabled)
        }

        is ItemEditAction.RepeatSelected -> {
            copy(repeatRule = action.repeatRule)
        }

        is ItemEditAction.PinnedChanged -> {
            copy(isPinned = action.pinned)
        }
    }

private fun ItemEditState.reduce(action: ItemEditAction.PlanInput): ItemEditState =
    when (action) {
        ItemEditAction.DateCleared -> {
            copy(date = null)
        }

        is ItemEditAction.TimeSlotSelected -> {
            if (action.timeSlot ==
                TimeSlot.EXACT
            ) {
                copy(dialog = ItemEditDialog.TIME)
            } else {
                copy(timeSlot = action.timeSlot, time = null)
            }
        }

        is ItemEditAction.TimeSelected -> {
            copy(timeSlot = TimeSlot.EXACT, time = action.time, dialog = null)
        }

        is ItemEditAction.PlaceChanged -> {
            copy(place = action.place)
        }

        is ItemEditAction.ShowAsDdayChanged -> {
            copy(showAsDday = action.enabled)
        }
    }

/** 새 항목 기본값: 디데이는 기준일이 필수라 오늘(또는 캘린더에서 고른 날)로 채운다. 빠른 시작이면 카테고리 · 제목도 */
private fun initialState(
    key: ItemEditKey,
    today: LocalDate,
): ItemEditState {
    val presetDate = key.presetDate?.let(LocalDate::parse)
    val category = key.presetCategory ?: Category.ETC
    return ItemEditState(
        itemId = key.itemId,
        type = key.type,
        today = today,
        isLoading = key.itemId != null,
        title = key.presetTitle.orEmpty(),
        category = category,
        icon = category.defaultIcon,
        date = presetDate ?: today.takeIf { key.type == ItemType.DDAY },
    )
}

private fun Item.toState(
    today: LocalDate,
    reminders: List<ItemReminder>,
    pinned: Boolean,
): ItemEditState =
    ItemEditState(
        itemId = id,
        type = content.type,
        today = today,
        title = content.title,
        icon = ItemIcon.fromKey(content.iconKey),
        isIconChosen = true,
        color = itemColorFromKey(content.colorKey),
        category = content.category,
        date = content.date,
        countMode = content.countMode ?: CountMode.DDAY,
        countFirstDayAsOne = content.countFirstDayAsOne,
        repeatRule = content.repeatRule,
        timeSlot = content.timeSlot,
        time = content.time,
        place = content.place.orEmpty(),
        memo = content.memo.orEmpty(),
        showAsDday = content.showAsDday,
        reminderOffsets = reminders.map { it.offsetDays }.toSet(),
        isPinned = pinned,
        visibility = content.visibility,
        isVisibilityLocked = content.visibility == Visibility.SHARED,
        backgroundImageId = content.backgroundImageId,
    )

/** 타입별로 쓰지 않는 값은 비운다 (data-model-erd 항목 타입별 사용 컬럼). 화면에 없는 값은 [original]에서 유지 */
private fun ItemEditState.toContent(original: ItemContent?): ItemContent {
    val isDday = type == ItemType.DDAY
    return ItemContent(
        type = type,
        title = title.trim(),
        colorKey = color.name,
        iconKey = icon.name,
        visibility = visibility,
        coupleId =
            when (visibility) {
                Visibility.SHARED -> original?.coupleId ?: coupleId
                Visibility.PRIVATE -> null
            },
        category = category,
        date = date,
        countMode = countMode.takeIf { isDday },
        countFirstDayAsOne = isDday && countFirstDayAsOne,
        repeatRule = if (isDday) repeatRule else RepeatRule.NONE,
        timeSlot = timeSlot.takeIf { !isDday },
        time = time.takeIf { !isDday },
        place = place.trim().takeIf { !isDday && it.isNotEmpty() },
        memo = memo.trim().ifEmpty { null },
        isDone = original?.isDone ?: false,
        showAsDday = !isDday && showAsDday,
        // 배경 사진은 디데이만 (product-spec 4-5)
        backgroundImageId = backgroundImageId.takeIf { isDday },
    )
}
