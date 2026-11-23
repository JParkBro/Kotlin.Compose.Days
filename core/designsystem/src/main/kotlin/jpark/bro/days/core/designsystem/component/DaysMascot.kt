package jpark.bro.days.core.designsystem.component

import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.R
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.designsystem.preview.DaysPreviewSurface
import kotlinx.coroutines.delay

private const val SPRITE_PX = 80

/** 원 안에서 마스코트가 차지할 최대 비율. 상반신은 원 아래에 붙어 원 밖으로 잘리므로 꽉 채운다 */
private const val CONTENT_RATIO = 0.8f
private const val BUST_CONTENT_RATIO = 1f

private const val BOUNCE_FRAME_MILLIS = 300L

/**
 * primary-soft 원 안의 마스코트. 다크 모드에서도 외곽선(#150F33)이 보이도록 원을 깐다.
 * 픽셀이 고르게 보이도록 원 크기 안에 들어가는 가장 큰 정수 배율로, 보간 없이 그린다.
 * 표정([Mascot.isBust])은 원 아래에 붙여 잘린 아랫부분이 원 테두리에 가려지게 한다.
 * 옆 글자가 상황을 설명하면 [contentDescription]은 null.
 */
@Composable
fun DaysMascot(
    mascot: Mascot,
    size: Dp,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    MascotFrame(
        frame = mascot.res,
        size = size,
        contentDescription = contentDescription,
        modifier = modifier,
        bust = mascot.isBust,
    )
}

/** 불러오는 중: 마스코트가 통통 튄다. 시스템 애니메이션을 끄면 멈춘 그림 */
@Composable
fun DaysMascotLoading(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    val context = LocalContext.current
    val inspection = LocalInspectionMode.current
    val animate =
        remember {
            !inspection &&
                Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
        }
    var squashed by remember { mutableStateOf(false) }
    if (animate) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(BOUNCE_FRAME_MILLIS)
                squashed = !squashed
            }
        }
    }
    MascotFrame(
        frame = if (squashed) R.drawable.mascot_bounce_f2 else R.drawable.mascot_bounce_f1,
        size = size,
        contentDescription = stringResource(R.string.common_loading),
        modifier = modifier.semantics { progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate },
    )
}

@Composable
private fun MascotFrame(
    @DrawableRes frame: Int,
    size: Dp,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    bust: Boolean = false,
) {
    val density = LocalDensity.current
    val ratio = if (bust) BUST_CONTENT_RATIO else CONTENT_RATIO
    val spriteSize =
        with(density) {
            val scale = ((size.toPx() * ratio) / SPRITE_PX).toInt().coerceAtLeast(1)
            (SPRITE_PX * scale).toDp()
        }
    Box(
        modifier =
            modifier
                .size(size)
                .clip(AppShapes.full)
                .background(AppThemeTokens.colors.primarySoft),
        contentAlignment = if (bust) Alignment.BottomCenter else Alignment.Center,
    ) {
        Image(
            bitmap = ImageBitmap.imageResource(frame),
            contentDescription = contentDescription,
            filterQuality = FilterQuality.None,
            modifier = Modifier.size(spriteSize),
        )
    }
}

@ComponentPreviews
@Composable
private fun DaysMascotPreview() {
    DaysPreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            DaysMascotLoading()
            DaysMascot(mascot = Mascot.WAVE, size = 96.dp, contentDescription = null)
            DaysMascot(mascot = Mascot.FACE_WORRIED, size = 64.dp, contentDescription = null)
        }
    }
}
