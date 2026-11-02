package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.HomeWidgets
import jpark.bro.days.core.model.WidgetConfig
import jpark.bro.days.core.model.WidgetSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeHomeWidgets(
    placed: List<WidgetConfig> = emptyList(),
    var pinSupported: Boolean = true,
) : HomeWidgets {
    val placed = MutableStateFlow(placed)

    /** requestPin으로 받은 (itemId, size) */
    val pinRequests = mutableListOf<Pair<String?, WidgetSize>>()

    /** openConfigure로 연 위젯 id */
    val openedConfigs = mutableListOf<Int>()

    /** false면 requestPin이 실패한다 */
    var pinResult: Boolean = true

    override fun observePlacedWidgets(): Flow<List<WidgetConfig>> = placed

    override fun isPinSupported(): Boolean = pinSupported

    override fun requestPin(
        itemId: String?,
        size: WidgetSize,
    ): Boolean {
        pinRequests += itemId to size
        return pinResult
    }

    override fun openConfigure(widgetId: Int) {
        openedConfigs += widgetId
    }
}
