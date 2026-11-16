package jpark.bro.days.core.ui

import jpark.bro.days.core.model.ImageCrop

/** 자르기 화면에서 확대할 수 있는 최대 배율 */
internal const val CROP_MAX_ZOOM = 4f

/**
 * 자르기 화면에서 사진을 옮기고 확대한 상태. 길이 단위는 정사각형 틀의 한 변(= 1)이다.
 * [scale]이 1이면 사진의 짧은 변이 틀에 꼭 맞고, [offsetX] · [offsetY]는 사진 중심이 틀 중심에서 벗어난 거리다.
 */
internal data class CropTransform(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
) {
    /** 손가락으로 옮기고([panX] · [panY]) 벌린([zoom]) 뒤의 상태. 틀 안에 빈 곳이 생기지 않게 막는다 */
    fun moved(
        aspect: Float,
        panX: Float,
        panY: Float,
        zoom: Float,
    ): CropTransform {
        val newScale = (scale * zoom).coerceIn(1f, CROP_MAX_ZOOM)
        val maxX = (photoWidth(aspect, newScale) - 1f) / 2f
        val maxY = (photoHeight(aspect, newScale) - 1f) / 2f
        return CropTransform(
            scale = newScale,
            offsetX = (offsetX + panX).coerceIn(-maxX, maxX),
            offsetY = (offsetY + panY).coerceIn(-maxY, maxY),
        )
    }

    /** 틀 안에 보이는 영역을 사진 크기에 대한 비율로 */
    fun toCrop(aspect: Float): ImageCrop {
        val width = photoWidth(aspect, scale)
        val height = photoHeight(aspect, scale)
        return ImageCrop(
            left = ((width - 1f) / 2f - offsetX) / width,
            top = ((height - 1f) / 2f - offsetY) / height,
            width = 1f / width,
            height = 1f / height,
        )
    }
}

/** [aspect] = 사진 가로 ÷ 세로. 짧은 변이 틀을 채우므로 긴 변은 틀보다 길다 */
private fun photoWidth(
    aspect: Float,
    scale: Float,
): Float = maxOf(1f, aspect) * scale

private fun photoHeight(
    aspect: Float,
    scale: Float,
): Float = maxOf(1f, 1f / aspect) * scale
