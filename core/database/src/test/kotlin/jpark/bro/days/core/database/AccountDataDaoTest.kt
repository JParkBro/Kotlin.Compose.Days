package jpark.bro.days.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.database.entity.ActivityLogEntity
import jpark.bro.days.core.database.entity.ImageEntity
import jpark.bro.days.core.database.entity.SyncCursorEntity
import jpark.bro.days.core.database.entity.UserEntity
import jpark.bro.days.core.database.entity.UserItemPrefEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/** 회원 탈퇴: 이 기기에서 그 계정의 데이터만 지운다 */
@RunWith(AndroidJUnit4::class)
internal class AccountDataDaoTest : DatabaseTest() {
    private val dao get() = db.accountDataDao()
    private val other = "user-other"

    private fun image(
        id: String,
        owner: String,
        remoteCoupleId: String? = null,
    ) = ImageEntity(id, owner, remoteCoupleId, "images/$id.webp", "NONE", 1080, 720, 150_000, NOW, deletedAt = null)

    private suspend fun seed() {
        db.coupleDao().upsertCouple(testCouple())
        db.itemDao().upsertItem(testItem("mine"))
        db.itemDao().upsertItem(testSharedItem("shared-by-partner", ownerUserId = PARTNER))
        db.itemDao().upsertItem(testItem("other").copy(ownerUserId = other, updatedBy = other))
        db.userItemPrefDao().upsertPref(UserItemPrefEntity(ME, "mine", isPinned = true, updatedAt = NOW))
        db.userItemPrefDao().upsertPref(UserItemPrefEntity(other, "other", isPinned = true, updatedAt = NOW))
        db.itemReminderDao().insertReminders(
            listOf(testReminder("r1", "mine", 0), testReminder("r2", "other", 0, userId = other)),
        )
        db.activityLogDao().insertActivities(
            listOf(
                ActivityLogEntity(id = "a1", userId = ME, type = "ITEM_ADDED", createdAt = NOW),
                ActivityLogEntity(id = "a2", userId = other, type = "ITEM_ADDED", createdAt = NOW),
            ),
        )
        db.imageDao().upsertImage(image("my-photo", ME, remoteCoupleId = COUPLE))
        db.imageDao().upsertImage(image("partner-photo", PARTNER, remoteCoupleId = COUPLE))
        db.imageDao().upsertImage(image("other-photo", other))
        db.anniversaryHideDao().upsertHide(testHide("DAY_100"))
        db.syncCursorDao().upsertCursor(SyncCursorEntity("items:$COUPLE", 1))
        db.syncCursorDao().upsertCursor(SyncCursorEntity("items:couple-other", 1))
        db.userDao().upsertUser(UserEntity(ME, "현우", null, NOW, null, "SYNCED"))
        db.userDao().upsertUser(UserEntity(PARTNER, "지민", null, NOW, null, "SYNCED"))
        db.userDao().upsertUser(UserEntity(other, "다른 계정", null, NOW, null, "SYNCED"))
    }

    @Test
    fun `내 커플과 올린 사진, 지울 사진 파일을 찾음`() =
        runTest {
            seed()
            assertEquals(listOf(COUPLE), db.coupleDao().getCoupleIds(ME))
            assertEquals(listOf("my-photo"), db.imageSyncDao().getUploadedImages(ME).map { it.id })
            assertEquals(
                setOf("images/my-photo.webp", "images/partner-photo.webp"),
                db.imageDao().getAccountImagePaths(ME, listOf(COUPLE)).toSet(),
            )
        }

    @Test
    fun `탈퇴하면 내 데이터와 내 커플 데이터만 지우고 다른 계정은 둠`() =
        runTest {
            seed()
            dao.deleteAccountData(ME, db.coupleDao().getCoupleIds(ME))

            assertNull(db.itemDao().getItem("mine"))
            assertNull(db.itemDao().getItem("shared-by-partner"))
            assertNotNull(db.itemDao().getItem("other"))
            assertNull(db.coupleDao().getCouple(COUPLE))
            assertNull(db.imageDao().getImage("my-photo"))
            assertNull(db.imageDao().getImage("partner-photo"))
            assertNotNull(db.imageDao().getImage("other-photo"))
            assertNull(db.userDao().getUser(ME))
            assertNull(db.userDao().getUser(PARTNER))
            assertNotNull(db.userDao().getUser(other))
            assertNull(db.syncCursorDao().getCursor("items:$COUPLE"))
            assertNotNull(db.syncCursorDao().getCursor("items:couple-other"))
        }

    @Test
    fun `커플이 없어도 지움`() =
        runTest {
            db.itemDao().upsertItem(testItem("mine"))
            dao.deleteAccountData(ME, db.coupleDao().getCoupleIds(ME))
            assertNull(db.itemDao().getItem("mine"))
        }
}
