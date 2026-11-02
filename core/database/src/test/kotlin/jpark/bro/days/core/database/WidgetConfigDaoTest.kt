package jpark.bro.days.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.days.core.database.entity.WidgetConfigEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class WidgetConfigDaoTest : DatabaseTest() {
    private val dao get() = db.widgetConfigDao()

    private fun config(
        widgetId: Int,
        itemId: String? = null,
    ) = WidgetConfigEntity(widgetId = widgetId, itemId = itemId, size = "SMALL", backgroundType = "THEME")

    @Test
    fun `같은 위젯 id로 저장하면 덮어씀`() =
        runTest {
            dao.upsertConfig(config(7, itemId = "discharge"))
            dao.upsertConfig(config(7, itemId = "toeic"))

            assertEquals("toeic", dao.getConfig(7)?.itemId)
        }

    @Test
    fun `지운 위젯의 설정만 사라짐`() =
        runTest {
            dao.upsertConfig(config(1))
            dao.upsertConfig(config(2))
            dao.upsertConfig(config(3))

            dao.deleteConfigs(listOf(1, 3))

            assertEquals(listOf(2), dao.observeConfigs().first().map { it.widgetId })
            assertNull(dao.getConfig(1))
        }

    @Test
    fun `기본값 저장은 이미 고른 설정을 덮어쓰지 않음`() =
        runTest {
            dao.upsertConfig(config(7, itemId = "discharge"))

            dao.insertConfigIfAbsent(config(7))
            dao.insertConfigIfAbsent(config(8))

            assertEquals("discharge", dao.getConfig(7)?.itemId)
            assertEquals(listOf(7, 8), dao.observeConfigs().first().map { it.widgetId })
        }
}
