package jpark.bro.days.feature.settings.impl.widgetsettings

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.HomeWidgets
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

internal class WidgetSettingsViewModel(
    private val homeWidgets: HomeWidgets,
    itemRepository: ItemRepository,
) : MviViewModel<WidgetSettingsState, WidgetSettingsAction, WidgetSettingsEvent>(WidgetSettingsState()) {
    init {
        combine(homeWidgets.observePlacedWidgets(), itemRepository.observeItems()) { widgets, items ->
            val titles = items.associate { it.id to it.content.title }
            setState {
                copy(
                    isLoading = false,
                    widgets =
                        widgets.map { config ->
                            val title = config.itemId?.let(titles::get)
                            WidgetRow(
                                config = config,
                                itemTitle = title,
                                isMissing =
                                    config.itemId != null && title == null,
                            )
                        },
                    canAddWidget = homeWidgets.isPinSupported(),
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: WidgetSettingsAction) {
        when (action) {
            WidgetSettingsAction.BackClicked -> {
                sendEvent(WidgetSettingsEvent.NavigateBack)
            }

            is WidgetSettingsAction.WidgetClicked -> {
                homeWidgets.openConfigure(action.widgetId)
            }

            WidgetSettingsAction.AddClicked -> {
                setState { copy(isSizeSheetOpen = true) }
            }

            is WidgetSettingsAction.SizeSelected -> {
                setState { copy(isSizeSheetOpen = false) }
                if (!homeWidgets.requestPin(itemId = null, size = action.size)) sendEvent(WidgetSettingsEvent.AddFailed)
            }

            WidgetSettingsAction.SizeSheetDismissed -> {
                setState { copy(isSizeSheetOpen = false) }
            }
        }
    }
}
