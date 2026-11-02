package jpark.bro.days.feature.calendar.impl

import jpark.bro.days.core.model.ItemType
import java.time.LocalDate

internal sealed interface CalendarAction {
    data object PreviousMonthClicked : CalendarAction

    data object NextMonthClicked : CalendarAction

    /** 이번 달로 돌아가 오늘을 고른다 */
    data object TodayClicked : CalendarAction

    /** 고른 날을 다시 누르면 선택이 풀린다 */
    data class DateClicked(
        val date: LocalDate,
    ) : CalendarAction

    /** 디데이는 상세로, 일정은 바로 수정으로 (product-spec 4-5) */
    data class ItemClicked(
        val id: String,
        val type: ItemType,
    ) : CalendarAction

    /** 일정 완료 체크 (product-spec 4-3) */
    data class DoneToggled(
        val id: String,
    ) : CalendarAction

    /** + 버튼 · "이 날 추가" */
    data object AddButtonClicked : CalendarAction

    data object AddSheetDismissed : CalendarAction

    /** 시트에서 고른 종류. 날짜를 골랐으면 그 날짜를 미리 채운다 (product-spec 4-2) */
    data class AddClicked(
        val type: ItemType,
    ) : CalendarAction
}
