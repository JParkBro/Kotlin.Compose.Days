package jpark.bro.days.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before

/** in-memory Room을 쓰는 DAO 테스트 공통 설정 */
internal abstract class DatabaseTest {
    protected lateinit var db: DaysDatabase

    @Before
    fun createDb() {
        db =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), DaysDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun closeDb() = db.close()
}
