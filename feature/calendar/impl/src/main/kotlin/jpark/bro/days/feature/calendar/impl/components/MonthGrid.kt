package jpark.bro.days.feature.calendar.impl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppColors
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.core.ui.toDisplayText
import jpark.bro.days.core.ui.toMonthDayText
import jpark.bro.days.feature.calendar.impl.CalendarDay
import jpark.bro.days.feature.calendar.impl.R
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import jpark.bro.days.core.designsystem.R as DesignR

/** 2026년 11월 · 이전 · 다음 달 · 오늘 칩 */
@Composable
internal fun MonthHeader(
    month: YearMonth,
    showTodayChip: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            text = month.toDisplayText(),
            style = AppThemeTokens.typography.headlineMd,
            color = AppThemeTokens.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        if (showTodayChip) {
            DaysSoftButton(
                text = stringResource(R.string.calendar_today),
                onClick = onToday,
                size = DaysButtonSize.SMALL,
                modifier = Modifier.testTag("calendar_today"),
            )
        }
        DaysIconButton(
            icon = DesignR.drawable.ic_chevron_left,
            contentDescription = stringResource(R.string.calendar_month_previous),
            onClick = onPrevious,
            modifier = Modifier.testTag("calendar_previous"),
        )
        DaysIconButton(
            icon = DesignR.drawable.ic_chevron_right,
            contentDescription = stringResource(R.string.calendar_month_next),
            onClick = onNext,
            modifier = Modifier.testTag("calendar_next"),
        )
    }
}

/**
 * 월 그리드 (theme-guide 5.7): 오늘은 primary 원 + on-primary, 선택한 날은 primary-soft 원,
 * 앞뒤 달은 text-tertiary, 일 · 토는 cal-sunday · cal-saturday. 디데이 primary 점, 일정 accent 점
 */
@Composable
internal fun MonthGrid(
    weeks: List<List<CalendarDay>>,
    onDateClick: (CalendarDay) -> Unit,
    modifier: Modifier = Modifier,
) {
    DaysCard(modifier = modifier.testTag("calendar_grid")) {
        Column(modifier = Modifier.padding(AppSpacing.sm)) {
            Row {
                WeekOrder.forEach { day ->
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                        style = AppThemeTokens.typography.labelMd,
                        color = AppThemeTokens.colors.weekdayColor(day) ?: AppThemeTokens.colors.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(vertical = AppSpacing.sm),
                    )
                }
            }
            weeks.forEach { week ->
                Row {
                    week.forEach { day ->
                        DayCell(day = day, onClick = { onDateClick(day) }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val description = day.date.toMonthDayText()
    val numberColor =
        when {
            day.isToday -> colors.onPrimary
            !day.inMonth -> colors.textTertiary
            else -> colors.weekdayColor(day.date.dayOfWeek) ?: colors.textPrimary
        }
    Column(
        modifier =
            modifier
                .selectable(selected = day.isSelected, enabled = day.inMonth, role = Role.Tab, onClick = onClick)
                .semantics { contentDescription = description }
                .padding(vertical = AppSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .clip(AppShapes.full)
                    .background(
                        when {
                            day.isToday -> colors.primary
                            day.isSelected -> colors.primarySoft
                            else -> Color.Transparent
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = AppThemeTokens.typography.labelLg.copy(fontFeatureSettings = "tnum"),
                color = numberColor,
            )
        }
        Row(modifier = Modifier.height(MarkerSize), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (day.hasDday) Marker(colors.primary)
            if (day.hasPlan) Marker(colors.accent)
        }
    }
}

@Composable
internal fun Marker(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(MarkerSize)
                .clip(AppShapes.full)
                .background(color),
    )
}

private val MarkerSize = 5.dp

/** 일요일 시작 */
private val WeekOrder: List<DayOfWeek> = listOf(DayOfWeek.SUNDAY) + DayOfWeek.entries.take(DayOfWeek.entries.size - 1)

private fun AppColors.weekdayColor(day: DayOfWeek): Color? =
    when (day) {
        DayOfWeek.SUNDAY -> calSunday
        DayOfWeek.SATURDAY -> calSaturday
        else -> null
    }
