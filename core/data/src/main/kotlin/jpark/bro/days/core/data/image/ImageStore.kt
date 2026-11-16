package jpark.bro.days.core.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.net.Uri
import jpark.bro.days.core.model.ImageCrop
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

/** 저장한 사진 정보 */
data class StoredImage(
    val width: Int,
    val height: Int,
    val bytes: Long,
)

/**
 * 기기에 두는 사진 파일 (`filesDir/images/{id}.webp`). 받은 사진도 여기에 영구 저장해 다시 받지 않는다
 * (infra-and-billing-protection.md 2-2). 고른 사진은 WebP · 긴 변 [MAX_EDGE]px로 줄여 Storage 규칙 한도(300KB) 안에 맞춘다.
 */
class ImageStore(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher,
) {
    fun relativePath(imageId: String): String = "$DIRECTORY/$imageId.webp"

    fun file(relativePath: String): File = File(context.filesDir, relativePath)

    /** 내려받기용 임시 파일. 다 받은 뒤 [file]로 옮긴다 (중간에 끊긴 파일을 사진으로 쓰지 않게) */
    fun tempFile(imageId: String): File = File(context.cacheDir, "$imageId.download")

    /**
     * 사진 선택기가 준 content Uri를 줄여 [relativePath]에 저장한다. 회전 정보(EXIF)는 디코더가 반영한다.
     * [crop]이 있으면 그 영역만 저장한다
     */
    suspend fun save(
        uri: String,
        relativePath: String,
        crop: ImageCrop? = null,
    ): StoredImage =
        withContext(ioDispatcher) {
            val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(uri))
            val bitmap =
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    if (crop == null) {
                        val (width, height) = scaledSize(info.size.width, info.size.height, MAX_EDGE)
                        decoder.setTargetSize(width, height)
                    } else {
                        // 자를 영역은 줄인 크기 기준 좌표로 넘긴다
                        val plan = cropPlan(info.size.width, info.size.height, crop, MAX_EDGE, MAX_DECODE_EDGE)
                        decoder.setTargetSize(plan.width, plan.height)
                        decoder.crop = Rect(plan.left, plan.top, plan.right, plan.bottom)
                    }
                    // 소프트웨어 비트맵이어야 compress할 수 있다
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            val stored =
                try {
                    val data = encodeWithinLimit(bitmap)
                    val target = file(relativePath)
                    target.parentFile?.mkdirs()
                    target.writeBytes(data)
                    StoredImage(width = bitmap.width, height = bitmap.height, bytes = data.size.toLong())
                } finally {
                    bitmap.recycle()
                }
            stored
        }

    suspend fun delete(relativePath: String) {
        withContext(ioDispatcher) { file(relativePath).delete() }
    }

    /** 품질을 낮춰 가며 [MAX_BYTES] 안에 맞춘다. 가장 낮은 품질로도 넘으면 실패 */
    private fun encodeWithinLimit(bitmap: Bitmap): ByteArray {
        var quality = START_QUALITY
        while (true) {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, out)
            val data = out.toByteArray()
            if (data.size <= MAX_BYTES) return data
            check(quality > MIN_QUALITY) { "사진을 줄이지 못했습니다." }
            quality = maxOf(MIN_QUALITY, quality - QUALITY_STEP)
        }
    }

    private companion object {
        const val DIRECTORY = "images"
        const val MAX_EDGE = 1080

        /** 자를 때 읽는 사진의 긴 변 한도. 자르기 전 전체를 메모리에 올리므로 큰 사진은 먼저 줄인다 */
        const val MAX_DECODE_EDGE = 2160
        const val START_QUALITY = 85
        const val MIN_QUALITY = 40
        const val QUALITY_STEP = 15

        /** Storage 규칙 300KB보다 여유를 둔다 */
        const val MAX_BYTES = 280 * 1024
    }
}

/** 긴 변이 [maxEdge]보다 크면 비율을 지켜 줄인다. 작은 사진은 키우지 않는다 */
internal fun scaledSize(
    width: Int,
    height: Int,
    maxEdge: Int,
): Pair<Int, Int> {
    val longEdge = maxOf(width, height)
    if (longEdge <= maxEdge) return width to height
    val scale = maxEdge.toDouble() / longEdge
    return maxOf(1, Math.round(width * scale).toInt()) to maxOf(1, Math.round(height * scale).toInt())
}

/** 자를 때 디코더에 넘길 값: 줄여 읽을 크기([width] × [height])와 그 안에서 남길 영역 */
internal data class CropPlan(
    val width: Int,
    val height: Int,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

/**
 * 자른 결과의 긴 변이 [maxEdge], 읽는 사진의 긴 변이 [maxDecodeEdge]를 넘지 않게 줄인다 (키우지 않음).
 * 영역이 사진 밖으로 나가면 안으로 맞추고, 1px보다 작아지지 않게 한다
 */
internal fun cropPlan(
    width: Int,
    height: Int,
    crop: ImageCrop,
    maxEdge: Int,
    maxDecodeEdge: Int,
): CropPlan {
    val cropLongEdge = maxOf(crop.width * width, crop.height * height).coerceAtLeast(1f)
    val scale = minOf(1f, maxEdge / cropLongEdge, maxDecodeEdge.toFloat() / maxOf(width, height))
    val scaledWidth = (width * scale).roundToInt().coerceAtLeast(1)
    val scaledHeight = (height * scale).roundToInt().coerceAtLeast(1)
    val left = (crop.left * scaledWidth).roundToInt().coerceIn(0, scaledWidth - 1)
    val top = (crop.top * scaledHeight).roundToInt().coerceIn(0, scaledHeight - 1)
    return CropPlan(
        width = scaledWidth,
        height = scaledHeight,
        left = left,
        top = top,
        right = ((crop.left + crop.width) * scaledWidth).roundToInt().coerceIn(left + 1, scaledWidth),
        bottom = ((crop.top + crop.height) * scaledHeight).roundToInt().coerceIn(top + 1, scaledHeight),
    )
}
