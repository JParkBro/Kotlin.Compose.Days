package jpark.bro.days.core.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

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

    /** 사진 선택기가 준 content Uri를 줄여 [relativePath]에 저장한다. 회전 정보(EXIF)는 디코더가 반영한다 */
    suspend fun save(
        uri: String,
        relativePath: String,
    ): StoredImage =
        withContext(ioDispatcher) {
            val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(uri))
            val bitmap =
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    val (width, height) = scaledSize(info.size.width, info.size.height, MAX_EDGE)
                    decoder.setTargetSize(width, height)
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
