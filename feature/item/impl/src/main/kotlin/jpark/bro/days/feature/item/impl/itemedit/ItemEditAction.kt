package jpark.bro.days.feature.item.impl.itemedit

import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.Category
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import jpark.bro.days.core.model.Visibility
import java.time.LocalDate
import java.time.LocalTime

internal sealed interface ItemEditAction {
    /** 디데이 · 일정 공통 입력 */
    sealed interface CommonInput : ItemEditAction

    /** 디데이만 쓰는 입력 */
    sealed interface DdayInput : ItemEditAction

    /** 일정만 쓰는 입력 */
    sealed interface PlanInput : ItemEditAction

    data class TitleChanged(
        val title: String,
    ) : CommonInput

    data class IconSelected(
        val icon: ItemIcon,
    ) : CommonInput

    data class ColorSelected(
        val color: AppThemeType,
    ) : CommonInput

    data class MemoChanged(
        val memo: String,
    ) : CommonInput

    data class ReminderToggled(
        val offsetDays: Int,
    ) : CommonInput

    /** 커플일 때만 보인다. 공유한 항목은 바꿀 수 없다 */
    data class VisibilitySelected(
        val visibility: Visibility,
    ) : CommonInput

    data class CategorySelected(
        val category: Category,
    ) : DdayInput

    data class CountModeSelected(
        val countMode: CountMode,
    ) : DdayInput

    data class CountFirstDayChanged(
        val enabled: Boolean,
    ) : DdayInput

    data class RepeatSelected(
        val repeatRule: RepeatRule,
    ) : DdayInput

    data class PinnedChanged(
        val pinned: Boolean,
    ) : DdayInput

    /** 일정은 날짜가 선택 사항이라 지울 수 있다 */
    data object DateCleared : PlanInput

    /** EXACT는 시각 선택 다이얼로그를 연다 */
    data class TimeSlotSelected(
        val timeSlot: TimeSlot,
    ) : PlanInput

    data class TimeSelected(
        val time: LocalTime,
    ) : PlanInput

    data class PlaceChanged(
        val place: String,
    ) : PlanInput

    data class ShowAsDdayChanged(
        val enabled: Boolean,
    ) : PlanInput

    data object DateClicked : ItemEditAction

    data class DateSelected(
        val date: LocalDate,
    ) : ItemEditAction

    data object DialogDismissed : ItemEditAction

    data object SaveClicked : ItemEditAction

    data object CancelClicked : ItemEditAction

    data object DeleteClicked : ItemEditAction

    data object DeleteConfirmed : ItemEditAction
}
