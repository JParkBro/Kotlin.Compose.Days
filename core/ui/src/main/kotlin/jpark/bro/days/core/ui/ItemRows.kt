package jpark.bro.days.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import jpark.bro.days.core.designsystem.component.DaysBadge
import jpark.bro.days.core.designsystem.component.DaysBadgeTone
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysItemIcon
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.component.DdayText
import jpark.bro.days.core.designsystem.component.DdayTone
import jpark.bro.days.core.domain.dday.isImminent
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import jpark.bro.days.core.model.itemColorFromKey
import java.time.LocalDate

/**
 * 디데이 한 줄 (홈 리스트 · 캘린더 공용): 아이콘 · 제목 · 날짜 · 반복 뱃지 · D-day.
 * 숫자는 7일 이내면 danger, 나머지는 text-primary (theme-guide 5.5). [date]는 반복 항목의 도래일처럼 기준일과 다를 때 넘긴다
 */
@Composable
fun DdayItemRow(
    content: ItemContent,
    dday: DdayCount,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    date: LocalDate? = content.date,
) {
    val dateText = date?.toDisplayText().orEmpty()
    DaysCard(modifier = modifier, onClick = onClick) {
        DaysListItem(
            title = content.title,
            subtitle = if (dday is DdayCount.Elapsed) stringResource(R.string.common_since, dateText) else dateText,
            leading = {
                DaysItemIcon(icon = ItemIcon.fromKey(content.iconKey), color = itemColorFromKey(content.colorKey))
            },
            trailing = {
                if (content.repeatRule != RepeatRule.NONE) {
                    DaysBadge(text = stringResource(content.repeatRule.label), tone = DaysBadgeTone.CATEGORY)
                }
                DdayText(count = dday, tone = if (dday.isImminent()) DdayTone.SOON else DdayTone.DEFAULT)
            },
        )
    }
}

/** 일정 시간 · 장소 한 줄: "2026.11.06 (금) · 저녁 · 성수동". 빠진 값은 건너뛴다 */
@Composable
fun ItemContent.scheduleText(
    showDate: Boolean = true,
    showPlace: Boolean = false,
): String {
    val slot = timeSlot
    val time = if (slot == TimeSlot.EXACT) time?.toDisplayText() else slot?.let { stringResource(it.label) }
    return listOfNotNull(date?.takeIf { showDate }?.toDisplayText(), time, place?.takeIf { showPlace })
        .joinToString(" · ")
}
