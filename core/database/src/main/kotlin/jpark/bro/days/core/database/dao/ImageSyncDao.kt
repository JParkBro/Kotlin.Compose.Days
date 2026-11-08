package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import jpark.bro.days.core.database.entity.ImageEntity
import kotlinx.coroutines.flow.Flow

/** 올릴 사진과 올릴 커플 */
data class ImageUpload(
    val imageId: String,
    val coupleId: String,
    val localPath: String,
)

/** 사진 동기화 전용 (core:sync). 화면 · 저장소용은 [ImageDao] */
@Dao
interface ImageSyncDao {
    /** 상대가 올린 사진을 받은 문서에서 알게 됨. 이미 있으면(내 사진 · 이미 받은 사진) 그대로 둔다 */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRemoteImage(image: ImageEntity)

    /**
     * [userId]가 연결된 커플에 올려야 할 사진: 그 커플의 공유 항목 배경이거나 내 프로필 사진인데
     * 아직 그 커플에 올리지 않은 것 (sync-and-pairing.md 6장 사진 동기화). 나만 보기 항목의 사진은 고르지 않는다
     */
    @Query(
        """
        SELECT i.id AS imageId, c.id AS coupleId, i.local_path AS localPath $TO_UPLOAD
        """,
    )
    suspend fun getImagesToUpload(userId: String): List<ImageUpload>

    /** [getImagesToUpload] 개수. 바뀌면 Push 작업을 예약한다 (커플 연결 직후 프로필 사진 등) */
    @Query(
        """
        SELECT COUNT(*) $TO_UPLOAD
        """,
    )
    fun observeUploadCount(userId: String): Flow<Int>

    /** 내가 올렸다가 더 쓰지 않게 된 사진. 서버 파일을 지운 뒤 행도 지운다 */
    @Query(
        """
        SELECT * FROM image
        WHERE deleted_at IS NOT NULL AND remote_couple_id IS NOT NULL AND owner_user_id = :userId
        """,
    )
    suspend fun getImagesToDelete(userId: String): List<ImageEntity>

    @Query("UPDATE image SET remote_couple_id = :coupleId, upload_state = 'NONE' WHERE id = :id")
    suspend fun markUploaded(
        id: String,
        coupleId: String,
    )

    @Query("UPDATE image SET upload_state = 'REJECTED' WHERE id = :id")
    suspend fun markRejected(id: String)

    /** 기기 파일이 사라짐 (사용자가 앱 데이터를 일부 지움 등). 다시 받을 수 있으면 받는다 */
    @Query("UPDATE image SET local_path = :localPath WHERE id = :id")
    suspend fun setLocalPath(
        id: String,
        localPath: String?,
    )

    @Query("DELETE FROM image WHERE id = :id")
    suspend fun deleteImage(id: String)

    /** [imageId]를 [userId]의 연결된 커플에 올렸는지 (프로필 문서에 사진 id를 적어도 되는지) */
    @Query(
        """
        SELECT EXISTS (
            SELECT 1 FROM image i JOIN couple c ON $ACTIVE_COUPLE_OF_USER
            WHERE i.id = :imageId AND i.remote_couple_id = c.id
        )
        """,
    )
    suspend fun isUploadedToActiveCouple(
        imageId: String,
        userId: String,
    ): Boolean

    /** 내가 커플 저장소에 올린 사진 (회원 탈퇴 때 서버 파일을 지운다) */
    @Query("SELECT * FROM image WHERE owner_user_id = :userId AND remote_couple_id IS NOT NULL")
    suspend fun getUploadedImages(userId: String): List<ImageEntity>
}

/** `c`가 [userId]의 연결된 커플 (`:userId` 필요) */
internal const val ACTIVE_COUPLE_OF_USER = """
    c.status = 'ACTIVE' AND (c.member1_user_id = :userId OR c.member2_user_id = :userId)
"""

/** 사진 `i`를 커플 `c`에 아직 올리지 않았고 올릴 수 있음 */
internal const val NEEDS_UPLOAD_TO_C = """
    i.deleted_at IS NULL AND i.local_path IS NOT NULL AND i.upload_state != 'REJECTED'
    AND (i.remote_couple_id IS NULL OR i.remote_couple_id != c.id)
"""

/**
 * [userId]의 연결된 커플 `c`에 올려야 할 사진 `i`: 그 커플의 공유 항목 배경이거나 내 프로필 사진 (`:userId` 필요)
 */
private const val TO_UPLOAD = """
        FROM image i
        JOIN couple c ON $ACTIVE_COUPLE_OF_USER
        WHERE $NEEDS_UPLOAD_TO_C AND (
            EXISTS (
                SELECT 1 FROM item t WHERE t.background_image_id = i.id AND t.visibility = 'SHARED'
                AND t.couple_id = c.id AND t.deleted_at IS NULL
            )
            OR EXISTS (SELECT 1 FROM user u WHERE u.id = :userId AND u.profile_image_id = i.id)
        )
"""
