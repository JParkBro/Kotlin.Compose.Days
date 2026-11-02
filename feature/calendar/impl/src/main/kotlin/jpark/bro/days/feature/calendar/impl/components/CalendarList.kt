package jpark.bro.days.feature.calendar.impl.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysBadge
import jpark.bro.days.core.designsystem.component.DaysBadgeTone
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysCheckbox
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.core.ui.scheduleText
import jpark.bro.days.core.ui.toMonthDayText
import jpark.bro.days.feature.calendar.impl.CalendarEntry
import jpark.bro.days.feature.calendar.impl.R
import java.time.LocalDate
import jpark.bro.days.core.designsystem.R as DesignR

/** 완료한 일정은 흐리게 */
private const val DONE_ALPHA = 0.5f

/** 날짜를 고르지 않았을 때: "이번 달 N건" + 범례 */
@Composable
internal fun MonthListHeader(
    count: Int,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            stringResource(R.string.calendar_list_month),
            style = AppThemeTokens.typography.headlineSm,
            color = colors.textPrimary,
        )
        DaysBadge(text = stringResource(R.string.calendar_list_count, count), tone = DaysBadgeTone.SHARED)
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Marker(colors.accent)
            Text(
                stringResource(R.string.calendar_legend_plan),
                style = AppThemeTokens.typography.labelSm,
                color = colors.textSecondary,
            )
            Marker(colors.primary, modifier = Modifier.padding(start = AppSpacing.xs))
            Text(
                stringResource(R.string.calendar_legend_dday),
                style = AppThemeTokens.typography.labelSm,
                color = colors.textSecondary,
            )
        }
    }
}

/** 날짜를 골랐을 때: "11월 6일 (금)" + 오늘 · 내일 + N건 + "이 날 추가" */
@Composable
internal fun DateListHeader(
    date: LocalDate,
    today: LocalDate,
    count: Int,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            text = date.toMonthDayText(),
            style = AppThemeTokens.typography.headlineSm,
            color = AppThemeTokens.colors.textPrimary,
        )
        when (date) {
            today -> DaysBadge(text = stringResource(R.string.calendar_today), tone = DaysBadgeTone.SHARED)

            today.plusDays(
                1,
            ),
            -> DaysBadge(text = stringResource(R.string.calendar_tomorrow), tone = DaysBadgeTone.CATEGORY)
        }
        Text(
            text = stringResource(R.string.calendar_list_count, count),
            style = AppThemeTokens.typography.labelMd,
            color = AppThemeTokens.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        DaysSoftButton(
            text = stringResource(R.string.calendar_list_add_for_date),
            onClick = onAddClick,
            size = DaysButtonSize.SMALL,
            icon = DesignR.drawable.ic_add,
            modifier = Modifier.testTag("calendar_add_for_date"),
        )
    }
}

/** 일정 한 줄: 완료 체크박스 · 제목 · 시간 · 장소 (product-spec 4-3) */
@Composable
internal fun PlanEntryRow(
    entry: CalendarEntry,
    showDate: Boolean,
    onClick: () -> Unit,
    onDoneToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = entry.item.content
    DaysCard(modifier = modifier.alpha(if (content.isDone) DONE_ALPHA else 1f), onClick = onClick) {
        DaysListItem(
            title = content.title,
            subtitle = content.scheduleText(showDate = showDate, showPlace = true).ifEmpty { null },
            leading = {
                DaysCheckbox(
                    checked = content.isDone,
                    onCheckedChange = { onDoneToggle() },
                    modifier = Modifier.testTag("calendar_done_${entry.item.id}"),
                )
            },
        )
    }
}

@Composable
internal fun EmptyListText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = AppThemeTokens.typography.bodyMd,
        color = AppThemeTokens.colors.textSecondary,
        modifier = modifier.fillMaxWidth().padding(vertical = AppSpacing.xl, horizontal = AppSpacing.xs),
    )
}
