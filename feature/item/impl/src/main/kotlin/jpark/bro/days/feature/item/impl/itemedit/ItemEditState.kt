package jpark.bro.days.feature.item.impl.itemedit

import jpark.bro.days.core.domain.dday.ddayOn
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.Category
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.ImageUploadQuota
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import jpark.bro.days.core.model.Visibility
import jpark.bro.days.core.model.defaultIcon
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

private const val SAME_DAY = 0
private const val ONE_DAY_BEFORE = 1
private const val ONE_WEEK_BEFORE = 7

/** 알림 선택지 (product-spec 4-5): 당일 · 1일 전 · 7일 전 */
internal val ReminderOffsets: List<Int> = listOf(SAME_DAY, ONE_DAY_BEFORE, ONE_WEEK_BEFORE)

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
    /** 배경 사진 id와 파일 (디데이만, product-spec 4-5) */
    val backgroundImageId: String? = null,
    val backgroundFile: File? = null,
    /** 사진 한도를 확인하거나 고른 사진을 줄이는 중 */
    val isPhotoWorking: Boolean = false,
    /** 커플 사진 한도를 다 써서 보여주는 시트 (null이면 닫힘) */
    val imageLimit: ImageUploadQuota? = null,
    /** 광고를 보는 중이거나 보상 반영을 기다리는 중 */
    val isRewardWorking: Boolean = false,
    /** 커플이 더 올릴 수 있는 사진 수. 우리 둘이 디데이일 때만 사진 칸 위에 보인다 (null이면 숨김 · 읽지 못함) */
    val photoQuotaRemaining: Int? = null,
    val dialog: ItemEditDialog? = null,
) {
    val showVisibility: Boolean get() = coupleId != null

    val isNew: Boolean get() = itemId == null

    /** 제목 필수, 디데이는 기준일 필수 (product-spec 2-1) */
    val canSave: Boolean
        get() =
            !isLoading && !isSaving && !isPhotoWorking && title.isNotBlank() && (type == ItemType.PLAN || date != null)

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
