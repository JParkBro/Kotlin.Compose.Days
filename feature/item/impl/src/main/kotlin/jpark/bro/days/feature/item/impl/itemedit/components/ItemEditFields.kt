package jpark.bro.days.feature.item.impl.itemedit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysItemIcon
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.component.DaysSwitch
import jpark.bro.days.core.designsystem.component.DdayText
import jpark.bro.days.core.designsystem.component.DdayTone
import jpark.bro.days.core.designsystem.component.iconRes
import jpark.bro.days.core.designsystem.component.itemColor
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.ui.toDisplayText
import jpark.bro.days.feature.item.impl.R
import java.time.LocalDate
import jpark.bro.days.core.designsystem.R as DesignR

/** 입력 묶음 제목 + 내용 */
@Composable
internal fun EditSection(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = AppThemeTokens.typography.labelMd,
                color = AppThemeTokens.colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                    style = AppThemeTokens.typography.bodySm,
                    color = AppThemeTokens.colors.textTertiary,
                )
            }
        }
        content()
    }
}

/** 맨 위 미리보기: 아이콘 · 제목 · 날짜 · D-day */
@Composable
internal fun ItemPreviewCard(
    icon: ItemIcon,
    color: AppThemeType,
    title: String,
    date: LocalDate?,
    dday: DdayCount?,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    DaysCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            DaysItemIcon(icon = icon, color = color, size = 48.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.ifBlank { stringResource(R.string.item_edit_preview_untitled) },
                    style = AppThemeTokens.typography.headlineSm,
                    color = if (title.isBlank()) colors.textTertiary else colors.textPrimary,
                    maxLines = 1,
                )
                Text(
                    text = date?.toDisplayText() ?: stringResource(R.string.item_edit_date_none),
                    style = AppThemeTokens.typography.bodySm,
                    color = colors.textSecondary,
                )
            }
            if (dday != null) DdayText(count = dday, tone = DdayTone.HIGHLIGHT)
        }
    }
}

/** 아이콘 8개 + 색 4개 */
@Composable
internal fun IconColorPicker(
    icon: ItemIcon,
    color: AppThemeType,
    onIconSelect: (ItemIcon) -> Unit,
    onColorSelect: (AppThemeType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    DaysCard(modifier = modifier) {
        Column(modifier = Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                ItemIcon.entries.forEach { option ->
                    val selected = option == icon
                    val description = stringResource(option.label)
                    Box(
                        modifier =
                            Modifier
                                .size(44.dp)
                                .clip(AppShapes.full)
                                .background(if (selected) colors.primarySoft else colors.fill)
                                .selectable(selected = selected, role = Role.RadioButton) { onIconSelect(option) }
                                .semantics { contentDescription = description }
                                .testTag("item_edit_icon_${option.name.lowercase()}"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(option.iconRes),
                            contentDescription = null,
                            tint = if (selected) colors.primary else colors.textSecondary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                AppThemeType.entries.forEach { option ->
                    val selected = option == color
                    val description = stringResource(option.colorLabel)
                    Box(
                        modifier =
                            Modifier
                                .size(36.dp)
                                .clip(AppShapes.full)
                                .background(itemColor(option))
                                .then(if (selected) Modifier.border(3.dp, colors.surface, AppShapes.full) else Modifier)
                                .selectable(selected = selected, role = Role.RadioButton) { onColorSelect(option) }
                                .semantics { contentDescription = description }
                                .testTag("item_edit_color_${option.name.lowercase()}"),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) {
                            Icon(
                                painter = painterResource(DesignR.drawable.ic_check),
                                contentDescription = null,
                                tint = colors.onPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 날짜 행: 누르면 날짜 선택, [onClear]가 있으면 지우기 버튼 */
@Composable
internal fun DateRow(
    label: String,
    date: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
) {
    DaysCard(modifier = modifier.testTag("item_edit_date")) {
        DaysListItem(
            title = date?.toDisplayText() ?: stringResource(R.string.item_edit_date_none),
            subtitle = label,
            onClick = onClick,
            showChevron = onClear == null || date == null,
            leading = {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_calendar),
                    contentDescription = null,
                    tint = AppThemeTokens.colors.primary,
                )
            },
            trailing =
                if (onClear != null && date != null) {
                    {
                        DaysIconButton(
                            icon = DesignR.drawable.ic_close,
                            contentDescription = stringResource(R.string.item_edit_date_clear),
                            onClick = onClear,
                        )
                    }
                } else {
                    null
                },
        )
    }
}

/** 제목 · 설명 + 스위치 행 */
@Composable
internal fun SwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    DaysCard(modifier = modifier) {
        DaysListItem(
            title = title,
            subtitle = description,
            trailing = { DaysSwitch(checked = checked, onCheckedChange = onCheckedChange) },
        )
    }
}

@Composable
internal fun ChipFlow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) { content() }
}
