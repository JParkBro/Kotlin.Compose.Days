package jpark.bro.days.core.data.mapper

import jpark.bro.days.core.model.WidgetBackground
import jpark.bro.days.core.model.WidgetConfig
import jpark.bro.days.core.model.WidgetSize
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetConfigMapperTest {
    @Test
    fun `위젯 설정을 Entity로 바꿨다가 되돌려도 값이 같음`() {
        val config =
            WidgetConfig(
                widgetId = 7,
                size = WidgetSize.MEDIUM,
                itemId = "discharge",
                background = WidgetBackground.DARK,
            )

        val entity = config.toEntity()

        assertEquals("MEDIUM", entity.size)
        assertEquals("DARK", entity.backgroundType)
        assertEquals(config, entity.toModel())
    }
}
