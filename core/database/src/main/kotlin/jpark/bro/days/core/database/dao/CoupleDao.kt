package jpark.bro.days.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import jpark.bro.days.core.database.entity.CoupleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoupleDao {
    /** [userId]의 연결된(ACTIVE) 커플. 없으면 null */
    @Query(
        """
        SELECT * FROM couple
        WHERE status = 'ACTIVE' AND (member1_user_id = :userId OR member2_user_id = :userId)
        LIMIT 1
        """,
    )
    fun observeActiveCouple(userId: String): Flow<CoupleEntity?>

    @Query(
        """
        SELECT * FROM couple
        WHERE status = 'ACTIVE' AND (member1_user_id = :userId OR member2_user_id = :userId)
        LIMIT 1
        """,
    )
    suspend fun getActiveCouple(userId: String): CoupleEntity?

    @Query("SELECT * FROM couple WHERE id = :id")
    suspend fun getCouple(id: String): CoupleEntity?

    @Upsert
    suspend fun upsertCouple(couple: CoupleEntity)

    /** [userId]가 멤버였던 모든 커플 (연결 · 해제 모두). 회원 탈퇴 때 지울 커플 */
    @Query("SELECT id FROM couple WHERE member1_user_id = :userId OR member2_user_id = :userId")
    suspend fun getCoupleIds(userId: String): List<String>
}
