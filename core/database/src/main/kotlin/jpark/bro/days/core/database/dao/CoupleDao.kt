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
}
