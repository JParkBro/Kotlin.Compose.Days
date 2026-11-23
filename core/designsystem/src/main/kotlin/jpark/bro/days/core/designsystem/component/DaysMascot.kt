package jpark.bro.days.core.designsystem.component

import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.R
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.designsystem.preview.DaysPreviewSurface

/** 원 안에서 마스코트가 차지할 최대 비율. 상반신은 원 아래에 붙어 원 밖으로 잘리므로 꽉 채운다 */
private const val CONTENT_RATIO = 0.8f
private const val BUST_CONTENT_RATIO = 1f

/** 로딩 애니메이션 (숨쉬기 시트: 8프레임 · 8fps, 한 바퀴 1초). 구독캘린더 SubCalMascotIdle과 같은 시트 */
private const val LOADING_FRAMES = 8
private const val LOADING_FRAME_MILLIS = 125

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
    MascotCircle(
        sheet = mascot.res,
        frameCount = 1,
        frame = 0,
        bust = mascot.isBust,
        modifier = modifier.size(size).mascotSemantics(contentDescription, loading = false),
    )
}

/** 불러오는 중: 마스코트가 숨쉬며 눈을 깜빡인다. 시스템 애니메이션을 끄면 첫 프레임에 멈춘다 */
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
    MascotCircle(
        sheet = R.drawable.mascot_breath_sheet_80x80_8f,
        frameCount = LOADING_FRAMES,
        frame = if (animate) rememberLoopFrame(LOADING_FRAMES, LOADING_FRAME_MILLIS) else 0,
        bust = false,
        modifier =
            modifier
                .size(size)
                .mascotSemantics(stringResource(R.string.common_loading), loading = true),
    )
}

/** 0 → [frameCount] - 1을 [frameMillis]마다 넘기며 끝없이 반복 */
@Composable
private fun rememberLoopFrame(
    frameCount: Int,
    frameMillis: Int,
): Int {
    val frame by rememberInfiniteTransition(label = "mascot").animateValue(
        initialValue = 0,
        targetValue = frameCount,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(tween(frameCount * frameMillis, easing = LinearEasing)),
        label = "frame",
    )
    return frame % frameCount
}

/** [availablePx] 안에 [spritePx] 그림이 들어가는 가장 큰 정수 배율. 작아도 1배는 그린다 */
internal fun mascotPixelScale(
    availablePx: Int,
    spritePx: Int,
): Int = (availablePx / spritePx).coerceAtLeast(1)

private fun Modifier.mascotSemantics(
    description: String?,
    loading: Boolean,
): Modifier =
    clearAndSetSemantics {
        if (description != null) contentDescription = description
        if (loading) progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
    }

/** 가로로 프레임이 붙은 [sheet]에서 [frame]번째를 primary-soft 원 안에 그린다 (상반신은 원 아래에 붙임) */
@Composable
private fun MascotCircle(
    @DrawableRes sheet: Int,
    frameCount: Int,
    frame: Int,
    bust: Boolean,
    modifier: Modifier,
) {
    val image = ImageBitmap.imageResource(sheet)
    val background = AppThemeTokens.colors.primarySoft
    Box(
        modifier =
            modifier
                .clip(AppShapes.full)
                .background(background)
                .drawBehind {
                    val frameWidth = image.width / frameCount
                    val ratio = if (bust) BUST_CONTENT_RATIO else CONTENT_RATIO
                    val available = (minOf(size.width, size.height) * ratio).toInt()
                    val scale = mascotPixelScale(available, maxOf(frameWidth, image.height))
                    val dst = IntSize(frameWidth * scale, image.height * scale)
                    val top = if (bust) size.height - dst.height else (size.height - dst.height) / 2
                    drawImage(
                        image = image,
                        srcOffset = IntOffset(frame * frameWidth, 0),
                        srcSize = IntSize(frameWidth, image.height),
                        dstOffset = IntOffset(((size.width - dst.width) / 2).toInt(), top.toInt()),
                        dstSize = dst,
                        filterQuality = FilterQuality.None,
                    )
                },
    )
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
