package jpark.bro.days.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.R
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.designsystem.preview.DaysPreviewSurface
import jpark.bro.days.core.model.DdayCount

/** 일반 카드: surface + border, 모서리 lg. [onClick]이 있으면 카드 전체를 누를 수 있다 */
@Composable
fun DaysCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AppThemeTokens.colors
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShapes.lg)
                .background(colors.surface)
                .border(1.dp, colors.border, AppShapes.lg)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        content = content,
    )
}

/** 섹션 제목 + 카드 묶음 (설정 메뉴 그룹 등). 카드 안 행 사이에는 [DaysDivider] */
@Composable
fun DaysSection(
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
        DaysCard(content = content)
    }
}

/** 리스트 아이콘 배경 (theme-guide.md 6장) */
enum class DaysIconBadgeTone {
    /** 공유 항목 · 강조 메뉴: primary-soft */
    SHARED,

    /** 반복: accent-soft */
    REPEAT,

    /** 임박: danger-soft */
    SOON,

    /** 기타: fill */
    DEFAULT,
}

/** 행 왼쪽 원형 아이콘 */
@Composable
fun DaysIconBadge(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    tone: DaysIconBadgeTone = DaysIconBadgeTone.DEFAULT,
) {
    val colors = AppThemeTokens.colors
    val (background, tint) =
        when (tone) {
            DaysIconBadgeTone.SHARED -> colors.primarySoft to colors.primary
            DaysIconBadgeTone.REPEAT -> colors.accentSoft to colors.onAccentSoft
            DaysIconBadgeTone.SOON -> colors.dangerSoft to colors.danger
            DaysIconBadgeTone.DEFAULT -> colors.fill to colors.textSecondary
        }
    Box(
        modifier =
            modifier
                .size(36.dp)
                .clip(AppShapes.full)
                .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

/**
 * 카드 · 리스트 안의 한 행: [leading] · 제목 · 설명 · [trailing] · 화살표.
 * [onClick]이 있으면 기본으로 오른쪽 화살표를 보여준다. [emphasized]면 제목을 굵게 (예: 개인정보 처리방침).
 */
@Composable
fun DaysListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = onClick != null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    emphasized: Boolean = false,
) {
    val colors = AppThemeTokens.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        leading?.invoke()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style =
                    AppThemeTokens.typography.labelLg.let {
                        if (emphasized) it.copy(fontWeight = FontWeight.Bold) else it
                    },
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(text = subtitle, style = AppThemeTokens.typography.bodySm, color = colors.textSecondary)
            }
        }
        trailing?.invoke(this)
        if (showChevron) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** 구분선 (border). 카드 안 행 사이는 [inset]으로 좌우를 띄운다 */
@Composable
fun DaysDivider(
    modifier: Modifier = Modifier,
    inset: Dp = 0.dp,
) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = inset),
        color = AppThemeTokens.colors.border,
    )
}

/** product-spec 9장 샘플 */
@ComponentPreviews
@Composable
private fun DaysCardPreview() {
    DaysPreviewSurface {
        DaysCard(onClick = {}) {
            DaysListItem(
                title = "지민 생일",
                subtitle = "2026.12.20 (일) · 매년",
                leading = { DaysIconBadge(icon = R.drawable.ic_cake, tone = DaysIconBadgeTone.REPEAT) },
                trailing = { DdayText(count = DdayCount.Remaining(45)) },
            )
            DaysDivider(inset = AppSpacing.marginMobile)
            DaysListItem(
                title = "성수 저녁",
                subtitle = "2026.11.06 (금) · 저녁 7시",
                leading = { DaysIconBadge(icon = R.drawable.ic_favorite, tone = DaysIconBadgeTone.SOON) },
                trailing = { DaysBadge(text = "우리 둘이", tone = DaysBadgeTone.SHARED) },
            )
            DaysDivider(inset = AppSpacing.marginMobile)
            DaysListItem(
                title = "금연",
                subtitle = "2026.08.07 (금)부터",
                leading = { DaysIconBadge(icon = R.drawable.ic_star) },
                trailing = { DdayText(count = DdayCount.Elapsed(91)) },
            )
        }
        DaysSection(title = "앱 설정") {
            DaysListItem(
                title = "알림",
                subtitle = "디데이 · 상대 활동 알림",
                onClick = {},
                leading = { DaysIconBadge(icon = R.drawable.ic_notifications, tone = DaysIconBadgeTone.SHARED) },
            )
        }
    }
}
