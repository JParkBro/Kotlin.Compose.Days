package jpark.bro.days.core.data.mapper

import jpark.bro.days.core.database.entity.WidgetConfigEntity
import jpark.bro.days.core.model.WidgetBackground
import jpark.bro.days.core.model.WidgetConfig
import jpark.bro.days.core.model.WidgetSize

internal fun WidgetConfigEntity.toModel(): WidgetConfig =
    WidgetConfig(
        widgetId = widgetId,
        size = WidgetSize.valueOf(size),
        itemId = itemId,
        background = WidgetBackground.valueOf(backgroundType),
        showProfile = showProfile,
    )

internal fun WidgetConfig.toEntity(): WidgetConfigEntity =
    WidgetConfigEntity(
        widgetId = widgetId,
        itemId = itemId,
        size = size.name,
        backgroundType = background.name,
        showProfile = showProfile,
    )
