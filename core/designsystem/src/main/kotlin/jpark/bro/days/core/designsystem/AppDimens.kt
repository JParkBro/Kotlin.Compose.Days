@file:Suppress("MagicNumber") // 토큰 정의 파일

package jpark.bro.days.core.designsystem

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 모서리 (theme-guide.md 10장, 값은 Stitch DESIGN.md rounded) */
@Immutable
object AppShapes {
    val sm: Shape = RoundedCornerShape(4.dp)
    val default: Shape = RoundedCornerShape(8.dp)
    val md: Shape = RoundedCornerShape(12.dp)
    val lg: Shape = RoundedCornerShape(16.dp)
    val xl: Shape = RoundedCornerShape(24.dp)
    val full: Shape = CircleShape
}

/** 간격 (theme-guide.md 10장, 값은 Stitch DESIGN.md spacing) */
@Immutable
object AppSpacing {
    /** 화면 좌우 여백 */
    val marginMobile: Dp = 16.dp
    val margin: Dp = 20.dp

    /** 카드 · 그리드 사이 */
    val gutter: Dp = 16.dp
    val gutterCompact: Dp = 12.dp

    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 14.dp
    val lg: Dp = 20.dp
    val xl: Dp = 28.dp
    val xxl: Dp = 40.dp
}
