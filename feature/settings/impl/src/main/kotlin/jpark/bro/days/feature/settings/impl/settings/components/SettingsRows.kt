package jpark.bro.days.feature.settings.impl.settings.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.R as DesignR

/** 섹션 제목 + 카드 묶음 */
@Composable
internal fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            text = title,
            style = AppThemeTokens.typography.labelMd,
            color = AppThemeTokens.colors.textSecondary,
            modifier = Modifier.padding(horizontal = AppSpacing.xs),
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.lg)
                    .background(AppThemeTokens.colors.surface),
            content = content,
        )
    }
}

/** 카드 안 행 사이 구분선 */
@Composable
internal fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = AppSpacing.marginMobile),
        color = AppThemeTokens.colors.border,
    )
}

@Composable
internal fun SettingsRow(
    @DrawableRes icon: Int,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = AppThemeTokens.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        IconBadge(icon = icon)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = AppThemeTokens.typography.labelLg, color = colors.textPrimary)
            if (subtitle != null) {
                Text(text = subtitle, style = AppThemeTokens.typography.bodySm, color = colors.textSecondary)
            }
        }
        trailing?.invoke()
        Icon(
            painter = painterResource(DesignR.drawable.ic_chevron_right),
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** 행 왼쪽 원형 아이콘 (primary-soft 배경) */
@Composable
internal fun IconBadge(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(36.dp)
                .clip(AppShapes.full)
                .background(AppThemeTokens.colors.primarySoft),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = AppThemeTokens.colors.primary,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** 점 + 글자 알약 (테마 이름, 연결 상태) */
@Composable
internal fun DotChip(
    text: String,
    dotColor: Color,
    textColor: Color,
    background: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(AppShapes.full)
                .background(background)
                .padding(horizontal = AppSpacing.sm + AppSpacing.xs, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs + 2.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .clip(AppShapes.full)
                    .background(dotColor),
        )
        Text(text = text, style = AppThemeTokens.typography.labelMd, color = textColor)
    }
}
