package jpark.bro.days.core.data.image

import jpark.bro.days.core.testing.FakeImageRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageDraftTest {
    private val images = FakeImageRepository()

    @Test
    fun `고르다 바꾼 사진은 바로 버리고 원래 사진은 저장 뒤에 버림`() =
        runTest {
            val draft = ImageDraft(images, original = "old")

            draft.replace("content://1")
            draft.replace("content://2")
            assertEquals(listOf("image-1"), images.discarded)

            draft.commit()
            assertEquals(listOf("image-1", "old"), images.discarded)
            assertEquals("image-2", draft.current)
        }

    @Test
    fun `저장하지 않고 나가면 새로 가져온 사진만 버리고 원래 사진은 그대로`() =
        runTest {
            val draft = ImageDraft(images, original = "old")
            draft.replace("content://1")

            draft.abandon()

            assertEquals(listOf("image-1"), images.discarded)
            assertEquals("old", draft.current)
        }

    @Test
    fun `원래 사진을 지우고 저장하면 원래 사진을 버림`() =
        runTest {
            val draft = ImageDraft(images, original = "old")

            draft.remove()
            assertEquals(emptyList<String>(), images.discarded)
            draft.commit()

            assertEquals(listOf("old"), images.discarded)
            assertNull(draft.current)
        }

    @Test
    fun `가져오기에 실패하면 지금 사진을 그대로 둠`() =
        runTest {
            val draft = ImageDraft(images, original = "old")
            images.failNextImport = true

            draft.replace("content://1")

            assertEquals("old", draft.current)
        }
}
