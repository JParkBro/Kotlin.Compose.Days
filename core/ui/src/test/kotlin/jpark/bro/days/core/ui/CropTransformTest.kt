package jpark.bro.days.core.ui

import jpark.bro.days.core.model.ImageCrop
import org.junit.Assert.assertEquals
import org.junit.Test

class CropTransformTest {
    private fun assertCrop(
        expected: ImageCrop,
        actual: ImageCrop,
    ) {
        assertEquals(expected.left, actual.left, TOLERANCE)
        assertEquals(expected.top, actual.top, TOLERANCE)
        assertEquals(expected.width, actual.width, TOLERANCE)
        assertEquals(expected.height, actual.height, TOLERANCE)
    }

    @Test
    fun `움직이지 않으면 가로 사진의 가운데 정사각형`() {
        assertCrop(ImageCrop(0.25f, 0f, 0.5f, 1f), CropTransform().toCrop(aspect = 2f))
    }

    @Test
    fun `움직이지 않으면 세로 사진의 가운데 정사각형`() {
        assertCrop(ImageCrop(0f, 0.25f, 1f, 0.5f), CropTransform().toCrop(aspect = 0.5f))
    }

    @Test
    fun `사진을 오른쪽으로 끌면 왼쪽 영역이 잡히고 끝을 넘지 않음`() {
        val moved = CropTransform().moved(aspect = 2f, panX = 0.3f, panY = 0.4f, zoom = 1f)
        assertCrop(ImageCrop(0.1f, 0f, 0.5f, 1f), moved.toCrop(aspect = 2f))

        val atEdge = moved.moved(aspect = 2f, panX = 5f, panY = 0f, zoom = 1f)
        assertCrop(ImageCrop(0f, 0f, 0.5f, 1f), atEdge.toCrop(aspect = 2f))
    }

    @Test
    fun `확대하면 영역이 줄고 배율은 1배에서 4배까지`() {
        val zoomed = CropTransform().moved(aspect = 1f, panX = 0f, panY = 0f, zoom = 2f)
        assertCrop(ImageCrop(0.25f, 0.25f, 0.5f, 0.5f), zoomed.toCrop(aspect = 1f))

        assertEquals(CROP_MAX_ZOOM, zoomed.moved(1f, 0f, 0f, zoom = 10f).scale, TOLERANCE)
        assertEquals(1f, zoomed.moved(1f, 0f, 0f, zoom = 0.1f).scale, TOLERANCE)
    }

    @Test
    fun `확대한 채 옮긴 뒤 축소하면 빈 곳이 생기지 않게 가운데로 돌아옴`() {
        val corner = CropTransform().moved(aspect = 1f, panX = 1f, panY = 1f, zoom = 2f)
        assertCrop(ImageCrop(0f, 0f, 0.5f, 0.5f), corner.toCrop(aspect = 1f))

        assertEquals(CropTransform(), corner.moved(aspect = 1f, panX = 0f, panY = 0f, zoom = 0.5f))
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
