package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

/**
 * 회원 탈퇴 때 이 기기에서 [userId] 계정의 데이터만 지운다 (sync-and-pairing.md 7장).
 * 한 기기에서 여러 계정을 쓸 수 있어 DB 전체를 지우지 않는다. 커플 id는 [CoupleDao.getCoupleIds], 사진 파일 경로는
 * [ImageDao.getAccountImagePaths]로 먼저 받아 둔다 (파일은 호출자가 지운다).
 */
@Dao
interface AccountDataDao {
    @Transaction
    suspend fun deleteAccountData(
        userId: String,
        coupleIds: List<String>,
    ) {
        deleteItems(userId, coupleIds)
        deleteUserItemPrefs(userId)
        deleteReminders(userId)
        deleteActivities(userId)
        deleteImages(userId, coupleIds)
        deleteAnniversaryHides(coupleIds)
        deleteUsers(userId, coupleIds)
        deleteCouples(coupleIds)
        coupleIds.forEach { coupleId -> deleteSyncCursors("%:$coupleId") }
    }

    /** 내 항목 + 내 커플의 공유 항목(상대가 만든 것 포함) */
    @Query("DELETE FROM item WHERE owner_user_id = :userId OR couple_id IN (:coupleIds)")
    suspend fun deleteItems(
        userId: String,
        coupleIds: List<String>,
    )

    @Query("DELETE FROM user_item_pref WHERE user_id = :userId")
    suspend fun deleteUserItemPrefs(userId: String)

    @Query("DELETE FROM item_reminder WHERE user_id = :userId")
    suspend fun deleteReminders(userId: String)

    @Query("DELETE FROM activity_log WHERE user_id = :userId")
    suspend fun deleteActivities(userId: String)

    @Query("DELETE FROM image WHERE owner_user_id = :userId OR remote_couple_id IN (:coupleIds)")
    suspend fun deleteImages(
        userId: String,
        coupleIds: List<String>,
    )

    @Query("DELETE FROM anniversary_hide WHERE couple_id IN (:coupleIds)")
    suspend fun deleteAnniversaryHides(coupleIds: List<String>)

    /** 내 프로필 + 내 커플 상대의 프로필 캐시 */
    @Query(
        """
        DELETE FROM user WHERE id = :userId OR id IN (
            SELECT member1_user_id FROM couple WHERE id IN (:coupleIds)
            UNION SELECT member2_user_id FROM couple WHERE id IN (:coupleIds)
        )
        """,
    )
    suspend fun deleteUsers(
        userId: String,
        coupleIds: List<String>,
    )

    @Query("DELETE FROM couple WHERE id IN (:coupleIds)")
    suspend fun deleteCouples(coupleIds: List<String>)

    /** 커서 키는 `{종류}:{coupleId}` (CoupleDataSync) */
    @Query("DELETE FROM sync_cursor WHERE `key` LIKE :pattern")
    suspend fun deleteSyncCursors(pattern: String)
}
