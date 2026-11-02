package jpark.bro.days.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.LocalIsDarkTheme
import jpark.bro.days.core.designsystem.R
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.designsystem.preview.DaysPreviewSurface
import jpark.bro.days.core.designsystem.themePalette
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.ItemIcon

/** 항목 아이콘 → drawable */
@get:DrawableRes
val ItemIcon.iconRes: Int
    get() =
        when (this) {
            ItemIcon.HEART -> R.drawable.ic_favorite
            ItemIcon.CAKE -> R.drawable.ic_cake
            ItemIcon.FLIGHT -> R.drawable.ic_flight
            ItemIcon.SCHOOL -> R.drawable.ic_school
            ItemIcon.STAR -> R.drawable.ic_star
            ItemIcon.EVENT -> R.drawable.ic_event
            ItemIcon.GIFT -> R.drawable.ic_redeem
            ItemIcon.FLAG -> R.drawable.ic_flag
        }

/** 항목 색 = 해당 테마의 primary. 지금 화면이 다크면 그 테마의 다크 팔레트 값을 쓴다 */
@Composable
fun itemColor(color: AppThemeType): Color = themePalette(color, LocalIsDarkTheme.current).primary

/** 항목 아이콘 배지: 항목 색 테마의 primary-soft 배경 + primary 아이콘 (홈 · 캘린더 · 편집 공용) */
@Composable
fun DaysItemIcon(
    icon: ItemIcon,
    color: AppThemeType,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val palette = themePalette(color, LocalIsDarkTheme.current)
    Box(
        modifier =
            modifier
                .size(size)
                .clip(AppShapes.full)
                .background(palette.primarySoft),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon.iconRes),
            contentDescription = null,
            tint = palette.primary,
            modifier = Modifier.size(size / 2),
        )
    }
}

@ComponentPreviews
@Composable
private fun DaysItemIconPreview() {
    DaysPreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            ItemIcon.entries.forEach { DaysItemIcon(icon = it, color = AppThemeType.PINK) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            AppThemeType.entries.forEach { DaysItemIcon(icon = ItemIcon.HEART, color = it) }
        }
    }
}
