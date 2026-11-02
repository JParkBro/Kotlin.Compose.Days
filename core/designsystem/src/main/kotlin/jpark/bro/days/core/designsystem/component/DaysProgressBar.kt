package jpark.bro.days.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.designsystem.preview.DaysPreviewSurface

/** 진행바 (다음 기념일까지). 트랙 fill, 채움 primary → accent 그라데이션 (theme-guide.md 5.6) */
@Composable
fun DaysProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
) {
    val colors = AppThemeTokens.colors
    val value = progress.coerceIn(0f, 1f)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height)
                .clip(AppShapes.full)
                .background(colors.fill)
                .semantics { progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f) },
    ) {
        if (value > 0f) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(value)
                        .fillMaxHeight()
                        .clip(AppShapes.full)
                        .background(Brush.horizontalGradient(listOf(colors.primary, colors.accent))),
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun DaysProgressBarPreview() {
    DaysPreviewSurface {
        DaysProgressBar(progress = 0f)
        DaysProgressBar(progress = 0.1f)
        DaysProgressBar(progress = 0.62f)
        DaysProgressBar(progress = 1f)
    }
}
