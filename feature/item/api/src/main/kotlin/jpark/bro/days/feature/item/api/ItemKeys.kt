package jpark.bro.days.feature.item.api

import androidx.navigation3.runtime.NavKey
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
 */
@Serializable
data class ItemEditKey(
    val itemId: String?,
    val type: ItemType,
    val presetDate: String?,
) : NavKey
