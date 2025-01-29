package jpark.bro.days.data.local

/*
@Dao
interface CoupleDateDao {

    @Insert
    suspend fun insertCoupleDate(coupleDateEntity: CoupleDateEntity): Long

    @Update
    suspend fun updateCoupleDate(coupleDateEntity: CoupleDateEntity)

    @Query("SELECT * FROM couple_date WHERE is_active = 1 LIMIT 1")
    suspend fun getActiveCoupleDate(): CoupleDateEntity?

    @Query("UPDATE couple_date SET couple_start_date = :startDate, updated_date = :updateDate WHERE uid = :id")
    suspend fun updateCoupleStartDateAndUpdateDate(id: Int, startDate: Date, updateDate: Date)

    @Query("UPDATE couple_date SET is_active = 0 WHERE uid = :id")
    suspend fun deactivateCoupleDate(id: Int)

}*/
