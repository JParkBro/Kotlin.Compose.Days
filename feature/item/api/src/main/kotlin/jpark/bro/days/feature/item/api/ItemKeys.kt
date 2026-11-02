package jpark.bro.days.feature.item.api

import androidx.navigation3.runtime.NavKey
import jpark.bro.days.core.model.Category
import jpark.bro.days.core.model.ItemType
import kotlinx.serialization.Serializable

/** 디데이 · 일정 상세 */
@Serializable
data class ItemDetailKey(
    val id: String,
) : NavKey

/**
 * 디데이 · 일정 추가(itemId = null) · 수정.
 * presetDate: 캘린더에서 선택한 날짜(yyyy-MM-dd)를 미리 채울 때.
 * presetCategory · presetTitle: 홈 빈 상태의 빠른 시작 칩(생일 · 여행 · 시험 · 전역일)으로 열 때.
 */
@Serializable
data class ItemEditKey(
    val itemId: String?,
    val type: ItemType,
    val presetDate: String?,
    val presetCategory: Category? = null,
    val presetTitle: String? = null,
) : NavKey
