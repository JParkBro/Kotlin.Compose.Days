package jpark.bro.days.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.database.dao.ImageUpload
import jpark.bro.days.core.database.entity.ImageEntity
import jpark.bro.days.core.database.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ImageDaoTest : DatabaseTest() {
    private val dao get() = db.imageDao()
    private val syncDao get() = db.imageSyncDao()

    private fun image(
        id: String,
        owner: String = ME,
        remoteCoupleId: String? = null,
        localPath: String? = "images/$id.webp",
    ) = ImageEntity(id, owner, remoteCoupleId, localPath, "NONE", 1080, 720, 150_000, NOW, deletedAt = null)

    private suspend fun seedProfile(imageId: String?) =
        db.userDao().upsertUser(UserEntity(ME, "현우", null, NOW, null, "SYNCED", profileImageId = imageId))

    @Test
    fun `연결된 커플의 공유 항목 배경과 내 프로필 사진만 올릴 대상`() =
        runTest {
            db.coupleDao().upsertCouple(testCouple())
            dao.upsertImage(image("shared-bg"))
            dao.upsertImage(image("private-bg"))
            dao.upsertImage(image("profile"))
            db.itemDao().upsertItem(testSharedItem("i1").copy(backgroundImageId = "shared-bg"))
            db.itemDao().upsertItem(testItem("i2").copy(backgroundImageId = "private-bg"))
            seedProfile("profile")

            assertEquals(
                setOf(
                    ImageUpload("shared-bg", COUPLE, "images/shared-bg.webp"),
                    ImageUpload("profile", COUPLE, "images/profile.webp"),
                ),
                syncDao.getImagesToUpload(ME).toSet(),
            )
            assertEquals(2, syncDao.observeUploadCount(ME).first())
        }

    @Test
    fun `솔로면 프로필 사진도 올리지 않음`() =
        runTest {
            dao.upsertImage(image("profile"))
            seedProfile("profile")

            assertEquals(emptyList<ImageUpload>(), syncDao.getImagesToUpload(ME))

            db.coupleDao().upsertCouple(testCouple(status = "DISCONNECTED"))
            assertEquals(emptyList<ImageUpload>(), syncDao.getImagesToUpload(ME))
        }

    @Test
    fun `이미 올렸거나 한도 초과 · 삭제 · 받기 전 사진은 다시 올리지 않음`() =
        runTest {
            db.coupleDao().upsertCouple(testCouple())
            dao.upsertImage(image("uploaded", remoteCoupleId = COUPLE))
            dao.upsertImage(image("rejected").copy(uploadState = "REJECTED"))
            dao.upsertImage(image("deleted").copy(deletedAt = NOW))
            dao.upsertImage(image("partner", owner = PARTNER, localPath = null))
            listOf("uploaded", "rejected", "deleted", "partner").forEachIndexed { index, imageId ->
                db.itemDao().upsertItem(testSharedItem("i$index").copy(backgroundImageId = imageId))
            }

            assertEquals(emptyList<ImageUpload>(), syncDao.getImagesToUpload(ME))
        }

    @Test
    fun `다른 커플에 올렸던 사진은 지금 커플에 다시 올림`() =
        runTest {
            db.coupleDao().upsertCouple(testCouple())
            dao.upsertImage(image("profile", remoteCoupleId = "couple-old"))
            seedProfile("profile")

            assertEquals(listOf(ImageUpload("profile", COUPLE, "images/profile.webp")), syncDao.getImagesToUpload(ME))

            syncDao.markUploaded("profile", COUPLE)
            assertTrue(syncDao.isUploadedToActiveCouple("profile", ME))
            assertEquals(0, syncDao.observeUploadCount(ME).first())
        }

    @Test
    fun `배경 사진을 올리기 전에는 공유 항목을 올리지 않음`() =
        runTest {
            db.coupleDao().upsertCouple(testCouple())
            dao.upsertImage(image("bg"))
            db.itemDao().upsertItem(testSharedItem("i1", syncState = "PENDING").copy(backgroundImageId = "bg"))
            db.itemDao().upsertItem(testSharedItem("i2", syncState = "PENDING"))

            assertEquals(listOf("i2"), db.itemSyncDao().getUnsyncedSharedItems(ME).map { it.id })

            syncDao.markUploaded("bg", COUPLE)
            assertEquals(
                setOf("i1", "i2"),
                db
                    .itemSyncDao()
                    .getUnsyncedSharedItems(ME)
                    .map { it.id }
                    .toSet(),
            )
        }

    @Test
    fun `한도를 넘어 올리지 못한 사진의 항목은 사진 없이 올림`() =
        runTest {
            db.coupleDao().upsertCouple(testCouple())
            dao.upsertImage(image("bg"))
            db.itemDao().upsertItem(testSharedItem("i1", syncState = "PENDING").copy(backgroundImageId = "bg"))

            syncDao.markRejected("bg")

            assertEquals(listOf("i1"), db.itemSyncDao().getUnsyncedSharedItems(ME).map { it.id })
        }

    @Test
    fun `지울 사진은 내가 올린 것 중 삭제 표시한 것만`() =
        runTest {
            dao.upsertImage(image("mine", remoteCoupleId = COUPLE).copy(deletedAt = NOW))
            dao.upsertImage(image("local-only").copy(deletedAt = NOW))
            dao.upsertImage(image("partner", owner = PARTNER, remoteCoupleId = COUPLE).copy(deletedAt = NOW))

            assertEquals(listOf("mine"), syncDao.getImagesToDelete(ME).map { it.id })
        }

    @Test
    fun `받은 사진 정보는 이미 있는 행을 덮지 않음`() =
        runTest {
            dao.upsertImage(image("bg"))

            syncDao.insertRemoteImage(image("bg", owner = PARTNER, remoteCoupleId = COUPLE, localPath = null))
            syncDao.insertRemoteImage(image("new", owner = PARTNER, remoteCoupleId = COUPLE, localPath = null))

            assertEquals("images/bg.webp", dao.getImage("bg")?.localPath)
            assertNull(dao.getImage("new")?.localPath)
            assertFalse(syncDao.isUploadedToActiveCouple("bg", ME))
        }

    @Test
    fun `프로필 사진을 올리면 내 프로필을 다시 올릴 대상으로`() =
        runTest {
            seedProfile("profile")

            db.userDao().markPendingIfProfileImage(ME, "other")
            assertEquals("SYNCED", db.userDao().getUser(ME)?.syncState)

            db.userDao().markPendingIfProfileImage(ME, "profile")
            assertEquals("PENDING", db.userDao().getUser(ME)?.syncState)
        }
}
