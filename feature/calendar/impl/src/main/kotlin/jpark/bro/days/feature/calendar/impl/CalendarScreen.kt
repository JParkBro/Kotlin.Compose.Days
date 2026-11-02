package jpark.bro.days.feature.calendar.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysFab
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.ui.AddItemSheet
import jpark.bro.days.core.ui.DdayItemRow
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.calendar.impl.components.DateListHeader
import jpark.bro.days.feature.calendar.impl.components.EmptyListText
import jpark.bro.days.feature.calendar.impl.components.MonthGrid
import jpark.bro.days.feature.calendar.impl.components.MonthHeader
import jpark.bro.days.feature.calendar.impl.components.MonthListHeader
import jpark.bro.days.feature.calendar.impl.components.PlanEntryRow
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun CalendarRoot(
    onNavigate: (NavKey) -> Unit,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val doneFailed = stringResource(R.string.calendar_done_failed)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is CalendarEvent.OpenItemDetail -> {
                onNavigate(ItemDetailKey(event.id))
            }

            is CalendarEvent.OpenItemEdit -> {
                onNavigate(ItemEditKey(itemId = event.itemId, type = event.type, presetDate = event.date?.toString()))
            }

            CalendarEvent.DoneToggleFailed -> {
                toastHostState.showSnackbar(doneFailed)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CalendarScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 캘린더 (product-spec 4-3): 월 이동 · 그리드 · 리스트 · + 버튼 */
@Composable
private fun CalendarScreen(
    state: CalendarState,
    onAction: (CalendarAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DaysTopBar(title = stringResource(R.string.calendar_main_title), isScrolled = listState.canScrollBackward)
            if (!state.isLoading) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding =
                        PaddingValues(
                            start = AppSpacing.marginMobile,
                            end = AppSpacing.marginMobile,
                            bottom = FabSpace,
                        ),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    calendarContent(state = state, onAction = onAction)
                }
            }
        }
        DaysFab(
            icon = DesignR.drawable.ic_add,
            contentDescription = stringResource(R.string.calendar_add),
            onClick = { onAction(CalendarAction.AddButtonClicked) },
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(AppSpacing.lg)
                    .testTag("calendar_fab"),
        )
    }
    if (state.isAddSheetOpen) {
        AddItemSheet(
            onSelect = { onAction(CalendarAction.AddClicked(it)) },
            onDismiss = { onAction(CalendarAction.AddSheetDismissed) },
        )
    }
}

private fun LazyListScope.calendarContent(
    state: CalendarState,
    onAction: (CalendarAction) -> Unit,
) {
    val selected = state.selectedDate
    val entries = state.calendar.entries
    item(key = "month_header") {
        MonthHeader(
            month = state.month,
            showTodayChip = state.showTodayChip,
            onPrevious = { onAction(CalendarAction.PreviousMonthClicked) },
            onNext = { onAction(CalendarAction.NextMonthClicked) },
            onToday = { onAction(CalendarAction.TodayClicked) },
        )
    }
    item(key = "grid") {
        MonthGrid(weeks = state.calendar.weeks, onDateClick = { onAction(CalendarAction.DateClicked(it.date)) })
    }
    item(key = "list_header") {
        if (selected != null) {
            DateListHeader(
                date = selected,
                today = state.today,
                count = entries.size,
                onAddClick = { onAction(CalendarAction.AddButtonClicked) },
                modifier = Modifier.padding(top = AppSpacing.sm),
            )
        } else {
            MonthListHeader(count = entries.size, modifier = Modifier.padding(top = AppSpacing.sm))
        }
    }
    if (entries.isEmpty()) {
        item(key = "empty") {
            EmptyListText(
                stringResource(
                    if (selected !=
                        null
                    ) {
                        R.string.calendar_list_empty_date
                    } else {
                        R.string.calendar_list_empty_month
                    },
                ),
            )
        }
    }
    items(items = entries, key = { it.key }) { entry ->
        CalendarEntryRow(entry = entry, showDate = selected == null, onAction = onAction)
    }
}

/** 디데이는 디데이 줄, 일정은 완료 체크박스 줄 */
@Composable
private fun CalendarEntryRow(
    entry: CalendarEntry,
    showDate: Boolean,
    onAction: (CalendarAction) -> Unit,
) {
    val content = entry.item.content
    val open = { onAction(CalendarAction.ItemClicked(entry.item.id, content.type)) }
    val dday = entry.dday
    val modifier = Modifier.testTag("calendar_entry_${entry.key}")
    when (content.type) {
        ItemType.DDAY -> {
            if (dday !=
                null
            ) {
                DdayItemRow(content = content, dday = dday, date = entry.date, onClick = open, modifier = modifier)
            }
        }

        ItemType.PLAN -> {
            PlanEntryRow(
                entry = entry,
                showDate = showDate,
                onClick = open,
                onDoneToggle = { onAction(CalendarAction.DoneToggled(entry.item.id)) },
                modifier = modifier,
            )
        }
    }
}

/** FAB(56dp) + 바깥 여백 */
private val FabSpace = 96.dp

@ScreenPreviews
@Composable
private fun CalendarMonthPreview() {
    AppTheme { CalendarScreen(state = previewCalendarState(selectedDate = null), onAction = {}) }
}

@ScreenPreviews
@Composable
private fun CalendarDateSelectedPreview() {
    AppTheme { CalendarScreen(state = previewCalendarState(selectedDate = PreviewTomorrow), onAction = {}) }
}
