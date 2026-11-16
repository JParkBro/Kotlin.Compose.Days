package jpark.bro.days.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysPrimaryButton
import jpark.bro.days.core.designsystem.component.DaysSecondaryButton
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.model.ImageCrop
import jpark.bro.days.core.designsystem.R as DesignR

/** 자르기 화면에 보여줄 사진의 짧은 변(px). 4배까지 확대해도 흐리지 않을 만큼만 읽는다 */
private const val PREVIEW_EDGE_PX = 1440

/**
 * 고른 사진에서 프로필 원에 들어갈 부분을 정하는 전체 화면 다이얼로그.
 * 사진을 끌어 옮기고 두 손가락으로 확대한다. 확인하면 원을 감싸는 정사각형 영역을 넘긴다.
 * [uri]는 사진 선택기가 준 content Uri
 */
@Composable
fun PhotoCropDialog(
    uri: String,
    onConfirm: (ImageCrop) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        PhotoCropContent(uri = uri, onConfirm = onConfirm, onDismiss = onDismiss)
    }
}

@Composable
private fun PhotoCropContent(
    uri: String,
    onConfirm: (ImageCrop) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    // 사진을 읽어야 비율을 안다. 그 전에는 확인할 수 없다
    var aspect by remember(uri) { mutableStateOf<Float?>(null) }
    var transform by remember(uri) { mutableStateOf(CropTransform()) }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colors.bg)
                .systemBarsPadding(),
    ) {
        Text(
            text = stringResource(R.string.common_photo_crop_title),
            style = typography.headlineSm,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md),
        )
        CropArea(
            uri = uri,
            aspect = aspect,
            transform = transform,
            onLoaded = { aspect = it },
            onGesture = { pan, zoom, frame ->
                aspect?.let { transform = transform.moved(it, pan.x / frame, pan.y / frame, zoom) }
            },
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.common_photo_crop_hint),
            style = typography.bodySm,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md),
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.marginMobile)
                    .padding(bottom = AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            DaysSecondaryButton(
                text = stringResource(DesignR.string.common_cancel),
                onClick = onDismiss,
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag("photo_crop_cancel"),
            )
            DaysPrimaryButton(
                text = stringResource(DesignR.string.common_confirm),
                onClick = { aspect?.let { onConfirm(transform.toCrop(it)) } },
                enabled = aspect != null,
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag("photo_crop_confirm"),
            )
        }
    }
}

/** 사진 + 원 밖을 가리는 막. 틀은 영역 가운데의 정사각형이고 원은 그 안에 꼭 맞는다 */
@Composable
private fun CropArea(
    uri: String,
    aspect: Float?,
    transform: CropTransform,
    onLoaded: (aspect: Float) -> Unit,
    onGesture: (pan: Offset, zoom: Float, framePx: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val context = LocalContext.current
    // 제스처 감지는 다시 시작하지 않으므로 최신 콜백을 따로 잡아 둔다
    val currentOnGesture by rememberUpdatedState(onGesture)
    val request =
        remember(uri) {
            ImageRequest
                .Builder(context)
                .data(uri)
                .size(PREVIEW_EDGE_PX)
                .build()
        }
    BoxWithConstraints(modifier = modifier.clipToBounds(), contentAlignment = Alignment.Center) {
        val frame = minOf(maxWidth, maxHeight) - AppSpacing.marginMobile * 2
        val framePx = with(LocalDensity.current) { frame.toPx() }
        val ratio = aspect ?: 1f
        AsyncImage(
            model = request,
            contentDescription = stringResource(R.string.common_photo_crop_photo),
            contentScale = ContentScale.FillBounds,
            onSuccess = { state ->
                val size = state.painter.intrinsicSize
                if (size.width > 0f && size.height > 0f) onLoaded(size.width / size.height)
            },
            modifier =
                Modifier
                    // 짧은 변이 틀에 맞는 크기. 영역보다 커도 줄이지 않고 가운데에 둔다
                    .requiredSize(frame * maxOf(1f, ratio), frame * maxOf(1f, 1f / ratio))
                    .graphicsLayer {
                        // 비율을 알기 전에는 찌그러져 보이므로 숨긴다
                        alpha = if (aspect == null) 0f else 1f
                        scaleX = transform.scale
                        scaleY = transform.scale
                        translationX = transform.offsetX * framePx
                        translationY = transform.offsetY * framePx
                    },
        )
        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(framePx) {
                        detectTransformGestures { _, pan, zoom, _ -> currentOnGesture(pan, zoom, framePx) }
                    }.testTag("photo_crop_area"),
        ) {
            val circle = Rect(center = center, radius = framePx / 2f)
            val outside =
                Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(Rect(Offset.Zero, Size(size.width, size.height)))
                    addOval(circle)
                }
            drawPath(path = outside, color = colors.overlay)
            drawCircle(
                color = colors.surface,
                radius = framePx / 2f,
                center = center,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun PhotoCropContentPreview() {
    AppTheme { PhotoCropContent(uri = "", onConfirm = {}, onDismiss = {}) }
}
