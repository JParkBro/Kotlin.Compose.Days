package jpark.bro.days.core.data.repository

import jpark.bro.days.core.common.suspendRunCatching
import jpark.bro.days.core.data.mapper.toEntity
import jpark.bro.days.core.data.mapper.toModel
import jpark.bro.days.core.database.dao.WidgetConfigDao
import jpark.bro.days.core.model.WidgetConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DefaultWidgetConfigRepository(
    private val widgetConfigDao: WidgetConfigDao,
) : WidgetConfigRepository {
    override fun observeConfigs(): Flow<List<WidgetConfig>> =
        widgetConfigDao.observeConfigs().map { configs -> configs.map { it.toModel() } }

    override suspend fun getConfig(widgetId: Int): WidgetConfig? = widgetConfigDao.getConfig(widgetId)?.toModel()

    override suspend fun saveConfig(config: WidgetConfig): Result<Unit> =
        suspendRunCatching {
            widgetConfigDao.upsertConfig(config.toEntity())
        }

    override suspend fun saveConfigIfAbsent(config: WidgetConfig): Result<Unit> =
        suspendRunCatching { widgetConfigDao.insertConfigIfAbsent(config.toEntity()) }

    override suspend fun deleteConfigs(widgetIds: List<Int>): Result<Unit> =
        suspendRunCatching { widgetConfigDao.deleteConfigs(widgetIds) }
}
