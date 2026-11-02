package jpark.bro.days.feature.item.impl.itemdetail

import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.Item

/** 타임라인 한 줄. [isNext]는 다가오는 것 중 첫 번째 (강조) */
internal data class DetailMilestone(
    val anniversary: AutoAnniversary,
    val dday: DdayCount,
    val isPassed: Boolean,
    val isNext: Boolean,
)

internal data class ItemDetailState(
    val id: String,
    val isLoading: Boolean = true,
    val item: Item? = null,
    val dday: DdayCount? = null,
    val milestones: List<DetailMilestone> = emptyList(),
    /** 런처가 앱에서 위젯 추가 요청을 받을 때만 "위젯 추가" (product-spec 4-4) */
    val canAddWidget: Boolean = false,
    val isWidgetSheetOpen: Boolean = false,
)
