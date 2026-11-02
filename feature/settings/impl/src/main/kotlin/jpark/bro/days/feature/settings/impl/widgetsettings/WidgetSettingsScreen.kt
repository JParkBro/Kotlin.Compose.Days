package jpark.bro.days.feature.settings.impl.widgetsettings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysDivider
import jpark.bro.days.core.designsystem.component.DaysIconBadge
import jpark.bro.days.core.designsystem.component.DaysIconBadgeTone
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysInfoBox
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.component.DaysPrimaryButton
import jpark.bro.days.core.designsystem.component.DaysSection
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.model.WidgetBackground
import jpark.bro.days.core.model.WidgetConfig
import jpark.bro.days.core.model.WidgetSize
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.core.ui.WidgetSizeSheet
import jpark.bro.days.core.ui.label
import jpark.bro.days.feature.settings.impl.R
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun WidgetSettingsRoot(
    onBack: () -> Unit,
    viewModel: WidgetSettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val addFailed = stringResource(R.string.settings_widget_add_failed)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            WidgetSettingsEvent.NavigateBack -> onBack()
            WidgetSettingsEvent.AddFailed -> toastHostState.showSnackbar(addFailed)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WidgetSettingsScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 위젯 설정 (product-spec 8장): 홈 화면에 놓인 위젯 목록 → 하나씩 설정. 없으면 추가 안내 */
@Composable
private fun WidgetSettingsScreen(
    state: WidgetSettingsState,
    onAction: (WidgetSettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg),
    ) {
        DaysTopBar(
            title = stringResource(R.string.settings_widget_title),
            isScrolled = scrollState.canScrollBackward,
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.settings_widget_back),
                    onClick = { onAction(WidgetSettingsAction.BackClicked) },
                )
            },
        )
        if (!state.isLoading) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = AppSpacing.marginMobile)
                        .padding(top = AppSpacing.sm, bottom = AppSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                if (state.widgets.isEmpty()) {
                    Text(
                        text = stringResource(R.string.settings_widget_empty),
                        style = AppThemeTokens.typography.headlineSm,
                        color = AppThemeTokens.colors.textPrimary,
                    )
                } else {
                    WidgetList(widgets = state.widgets, onClick = { onAction(WidgetSettingsAction.WidgetClicked(it)) })
                }
                DaysInfoBox(text = stringResource(R.string.settings_widget_guide))
                if (state.canAddWidget) {
                    DaysPrimaryButton(
                        text = stringResource(R.string.settings_widget_add),
                        onClick = { onAction(WidgetSettingsAction.AddClicked) },
                        icon = DesignR.drawable.ic_add,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .testTag("settings_widget_add"),
                    )
                }
            }
        }
    }
    if (state.isSizeSheetOpen) {
        WidgetSizeSheet(
            onSelect = { onAction(WidgetSettingsAction.SizeSelected(it)) },
            onDismiss = { onAction(WidgetSettingsAction.SizeSheetDismissed) },
        )
    }
}

@Composable
private fun WidgetList(
    widgets: List<WidgetRow>,
    onClick: (Int) -> Unit,
) {
    DaysSection(title = stringResource(R.string.settings_widget_list)) {
        widgets.forEachIndexed { index, row ->
            if (index > 0) DaysDivider(inset = AppSpacing.marginMobile)
            DaysListItem(
                title = stringResource(row.config.size.label),
                subtitle =
                    stringResource(
                        R.string.settings_widget_row_desc,
                        row.itemText(),
                        stringResource(row.config.background.label),
                    ),
                onClick = { onClick(row.config.widgetId) },
                leading = { DaysIconBadge(icon = DesignR.drawable.ic_widgets, tone = DaysIconBadgeTone.SHARED) },
                modifier = Modifier.testTag("settings_widget_${row.config.widgetId}"),
            )
        }
    }
}

@Composable
private fun WidgetRow.itemText(): String =
    when {
        isMissing -> stringResource(R.string.settings_widget_item_missing)
        itemTitle != null -> itemTitle
        else -> stringResource(R.string.settings_widget_item_auto)
    }

private val WidgetBackground.label: Int
    get() =
        when (this) {
            WidgetBackground.THEME -> R.string.settings_widget_background_theme
            WidgetBackground.DARK -> R.string.settings_widget_background_dark
            WidgetBackground.WHITE -> R.string.settings_widget_background_white
        }

@ScreenPreviews
@Composable
private fun WidgetSettingsPreview() {
    AppTheme {
        WidgetSettingsScreen(
            state =
                WidgetSettingsState(
                    isLoading = false,
                    widgets =
                        listOf(
                            WidgetRow(
                                WidgetConfig(1, WidgetSize.SMALL, itemId = "discharge"),
                                "전역일",
                                isMissing = false,
                            ),
                            WidgetRow(
                                WidgetConfig(2, WidgetSize.MEDIUM, background = WidgetBackground.DARK),
                                null,
                                isMissing = false,
                            ),
                        ),
                    canAddWidget = true,
                ),
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun WidgetSettingsEmptyPreview() {
    AppTheme {
        WidgetSettingsScreen(
            state = WidgetSettingsState(isLoading = false, canAddWidget = true),
            onAction = {},
        )
    }
}
