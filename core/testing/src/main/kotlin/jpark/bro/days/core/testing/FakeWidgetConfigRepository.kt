package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.WidgetConfigRepository
import jpark.bro.days.core.model.WidgetConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeWidgetConfigRepository(
    initial: List<WidgetConfig> = emptyList(),
) : WidgetConfigRepository {
    private val configs = MutableStateFlow(initial.associateBy { it.widgetId })

    override fun observeConfigs(): Flow<List<WidgetConfig>> = configs.map { all -> all.values.sortedBy { it.widgetId } }

    override suspend fun getConfig(widgetId: Int): WidgetConfig? = configs.value[widgetId]

    override suspend fun saveConfig(config: WidgetConfig): Result<Unit> {
        configs.update { it + (config.widgetId to config) }
        return Result.success(Unit)
    }

    override suspend fun saveConfigIfAbsent(config: WidgetConfig): Result<Unit> {
        configs.update { if (config.widgetId in it) it else it + (config.widgetId to config) }
        return Result.success(Unit)
    }

    override suspend fun deleteConfigs(widgetIds: List<Int>): Result<Unit> {
        configs.update { it - widgetIds.toSet() }
        return Result.success(Unit)
    }
}
