package jpark.bro.days.feature.item.impl.itemedit

import jpark.bro.days.core.domain.dday.ddayOn
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.Category
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import jpark.bro.days.core.model.Visibility
import jpark.bro.days.core.model.defaultIcon
import java.time.LocalDate
import java.time.LocalTime

private const val SAME_DAY = 0
private const val ONE_DAY_BEFORE = 1
private const val ONE_WEEK_BEFORE = 7
private const val REMINDER_HOUR = 9

/** 알림 선택지 (product-spec 4-5): 당일 · 1일 전 · 7일 전 */
internal val ReminderOffsets: List<Int> = listOf(SAME_DAY, ONE_DAY_BEFORE, ONE_WEEK_BEFORE)

/** 알림 발송 시각: 오전 9시 (product-spec 4-5) */
internal val ReminderTime: LocalTime = LocalTime.of(REMINDER_HOUR, 0)

internal enum class ItemEditDialog {
    DATE,
    TIME,
    DELETE_CONFIRM,
}

/** 디데이 · 일정 추가 · 수정. [itemId]가 null이면 새로 만들기 */
internal data class ItemEditState(
    val itemId: String? = null,
    val type: ItemType = ItemType.DDAY,
    val today: LocalDate = LocalDate.MIN,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val title: String = "",
    val icon: ItemIcon = Category.ETC.defaultIcon,
    /** 사용자가 아이콘을 직접 골랐으면 카테고리를 바꿔도 아이콘을 유지한다 */
    val isIconChosen: Boolean = false,
    val color: AppThemeType = AppThemeType.PINK,
    val category: Category = Category.ETC,
    val date: LocalDate? = null,
    val countMode: CountMode = CountMode.DDAY,
    val countFirstDayAsOne: Boolean = false,
    val repeatRule: RepeatRule = RepeatRule.NONE,
    val timeSlot: TimeSlot? = null,
    val time: LocalTime? = null,
    val place: String = "",
    val memo: String = "",
    val showAsDday: Boolean = false,
    val reminderOffsets: Set<Int> = emptySet(),
    val isPinned: Boolean = false,
    /** 연결된 커플 id. 있을 때만 공개 범위를 고른다 (product-spec 4-5) */
    val coupleId: String? = null,
    val visibility: Visibility = Visibility.PRIVATE,
    /** 이미 공유한 항목은 나만 보기로 바꿀 수 없다 (product-spec 2-6) */
    val isVisibilityLocked: Boolean = false,
    val dialog: ItemEditDialog? = null,
) {
    val showVisibility: Boolean get() = coupleId != null

    val isNew: Boolean get() = itemId == null

    /** 제목 필수, 디데이는 기준일 필수 (product-spec 2-1) */
    val canSave: Boolean
        get() = !isLoading && !isSaving && title.isNotBlank() && (type == ItemType.PLAN || date != null)

    /** 위쪽 미리보기 카드의 D-day. 날짜가 없으면 null */
    val previewDday: DdayCount?
        get() =
            ItemContent(
                type = type,
                title = title,
                colorKey = color.name,
                iconKey = icon.name,
                date = date,
                countMode = countMode,
                countFirstDayAsOne = countFirstDayAsOne,
                repeatRule = repeatRule,
            ).ddayOn(today)
}
