package jpark.bro.days.core.model

import java.time.LocalDate
import java.time.LocalTime

/** 저장된 디데이 · 일정. 사용자가 입력하는 값은 [content], 나머지는 저장할 때 Repository가 채운다 */
data class Item(
    val id: String,
    val content: ItemContent,
    val ownerUserId: String,
    val updatedBy: String,
    /** epoch millis */
    val createdAt: Long,
    /** epoch millis */
    val updatedAt: Long,
)

/**
 * 사용자가 입력하는 항목 값 (product-spec 2장, data-model-erd 항목 타입별 사용 컬럼).
 * DDAY: date · countMode 필수. PLAN: date 선택, timeSlot · place · isDone 사용.
 */
data class ItemContent(
    val type: ItemType,
    val title: String,
    val colorKey: String,
    val iconKey: String,
    val visibility: Visibility = Visibility.PRIVATE,
    /** SHARED일 때만 */
    val coupleId: String? = null,
    val category: Category = Category.ETC,
    val date: LocalDate? = null,
    val countMode: CountMode? = null,
    val countFirstDayAsOne: Boolean = false,
    val repeatRule: RepeatRule = RepeatRule.NONE,
    val timeSlot: TimeSlot? = null,
    /** timeSlot이 EXACT일 때만 */
    val time: LocalTime? = null,
    val place: String? = null,
    val memo: String? = null,
    val isDone: Boolean = false,
    val showAsDday: Boolean = false,
    val backgroundImageId: String? = null,
)
