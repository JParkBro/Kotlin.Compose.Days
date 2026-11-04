package jpark.bro.days.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import jpark.bro.days.core.database.dao.AnniversaryHideDao
import jpark.bro.days.core.database.dao.CoupleDao
import jpark.bro.days.core.database.dao.ItemDao
import jpark.bro.days.core.database.dao.ItemReminderDao
import jpark.bro.days.core.database.dao.SyncCursorDao
import jpark.bro.days.core.database.dao.UserItemPrefDao
import jpark.bro.days.core.database.dao.WidgetConfigDao
import jpark.bro.days.core.database.entity.AnniversaryHideEntity
import jpark.bro.days.core.database.entity.CoupleEntity
import jpark.bro.days.core.database.entity.ItemEntity
import jpark.bro.days.core.database.entity.ItemReminderEntity
import jpark.bro.days.core.database.entity.SyncCursorEntity
import jpark.bro.days.core.database.entity.UserItemPrefEntity
import jpark.bro.days.core.database.entity.WidgetConfigEntity

/**
 * 출시 전까지 version 1을 고쳐 쓴다 (docs/days/README.md "공통 문서와 다르게 가는 부분").
 * 출시 후에는 version을 올리고 Migration + 마이그레이션 테스트를 추가한다.
 */
@Database(
    entities = [
        ItemEntity::class,
        UserItemPrefEntity::class,
        ItemReminderEntity::class,
        WidgetConfigEntity::class,
        CoupleEntity::class,
        AnniversaryHideEntity::class,
        SyncCursorEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class DaysDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    abstract fun userItemPrefDao(): UserItemPrefDao

    abstract fun itemReminderDao(): ItemReminderDao

    abstract fun widgetConfigDao(): WidgetConfigDao

    abstract fun coupleDao(): CoupleDao

    abstract fun anniversaryHideDao(): AnniversaryHideDao

    abstract fun syncCursorDao(): SyncCursorDao
}
