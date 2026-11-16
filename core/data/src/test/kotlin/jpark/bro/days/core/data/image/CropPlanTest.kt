package jpark.bro.days.core.data.image

import jpark.bro.days.core.model.ImageCrop
import org.junit.Assert.assertEquals
import org.junit.Test

class CropPlanTest {
    @Test
    fun `가로 사진 가운데 정사각형은 긴 변 1080으로 줄여 자름`() {
        // 4032 × 3024의 가운데 3024 × 3024
        val plan = cropPlan(4032, 3024, ImageCrop(0.125f, 0f, 0.75f, 1f), maxEdge = 1080, maxDecodeEdge = 2160)

        assertEquals(CropPlan(width = 1440, height = 1080, left = 180, top = 0, right = 1260, bottom = 1080), plan)
    }

    @Test
    fun `작은 사진은 키우지 않고 그 크기에서 자름`() {
        val plan = cropPlan(800, 600, ImageCrop(0.25f, 0f, 0.75f, 1f), maxEdge = 1080, maxDecodeEdge = 2160)

        assertEquals(CropPlan(width = 800, height = 600, left = 200, top = 0, right = 800, bottom = 600), plan)
    }

    @Test
    fun `크게 확대해 잘라도 읽는 사진은 긴 변 2160을 넘지 않음`() {
        // 8000 × 6000에서 1500 × 1500만: 결과 한도로는 줄일 필요가 적지만 읽기 한도로 줄인다
        val plan = cropPlan(8000, 6000, ImageCrop(0f, 0f, 0.1875f, 0.25f), maxEdge = 1080, maxDecodeEdge = 2160)

        assertEquals(CropPlan(width = 2160, height = 1620, left = 0, top = 0, right = 405, bottom = 405), plan)
    }

    @Test
    fun `사진 밖으로 나간 영역은 안으로 맞추고 1px보다 작아지지 않음`() {
        val plan = cropPlan(100, 100, ImageCrop(0.995f, -0.2f, 0.5f, 0.001f), maxEdge = 1080, maxDecodeEdge = 2160)

        assertEquals(CropPlan(width = 100, height = 100, left = 99, top = 0, right = 100, bottom = 1), plan)
    }
}
