package jpark.bro.days.widget.config

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.LocalIsDarkTheme
import jpark.bro.days.core.designsystem.component.DaysBadge
import jpark.bro.days.core.designsystem.component.DaysBadgeTone
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysItemIcon
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.component.DaysPrimaryButton
import jpark.bro.days.core.designsystem.component.DaysSection
import jpark.bro.days.core.designsystem.component.DaysSwitch
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.component.ddayLabel
import jpark.bro.days.core.designsystem.component.itemColor
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.WidgetBackground
import jpark.bro.days.core.model.itemColorFromKey
import jpark.bro.days.core.ui.DaysPhoto
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.core.ui.label
import jpark.bro.days.widget.R
import jpark.bro.days.widget.effective
import jpark.bro.days.widget.widgetColorScheme
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun WidgetConfigRoot(
    viewModel: WidgetConfigViewModel,
    onClose: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val saveFailed = stringResource(R.string.widget_config_save_failed)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            WidgetConfigEvent.Close -> onClose()
            WidgetConfigEvent.SaveFailed -> toastHostState.showSnackbar(saveFailed)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WidgetConfigScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 위젯 하나 설정: 미리보기 · 표시할 디데이 · 배경 · 저장 (product-spec 8장) */
@Composable
private fun WidgetConfigScreen(
    state: WidgetConfigState,
    onAction: (WidgetConfigAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .statusBarsPadding(),
    ) {
        DaysTopBar(
            title = stringResource(R.string.widget_config_title),
            isScrolled = scrollState.canScrollBackward,
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_close,
                    contentDescription = stringResource(R.string.widget_config_close),
                    onClick = { onAction(WidgetConfigAction.CloseClicked) },
                )
            },
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            PreviewArea(state)
            if (!state.isLoading) {
                ItemChoices(state = state, onAction = onAction)
                BackgroundChoices(state = state, onAction = onAction)
                DaysCard {
                    DaysListItem(
                        title = stringResource(R.string.widget_config_profile),
                        subtitle = stringResource(R.string.widget_config_profile_desc),
                        trailing = {
                            DaysSwitch(
                                checked = state.showProfile,
                                onCheckedChange = { onAction(WidgetConfigAction.ShowProfileChanged(it)) },
                                modifier = Modifier.testTag("widget_config_profile"),
                            )
                        },
                    )
                }
            }
        }
        DaysPrimaryButton(
            text = stringResource(R.string.widget_config_save),
            onClick = { onAction(WidgetConfigAction.SaveClicked) },
            enabled = !state.isLoading && !state.isSaving,
            icon = DesignR.drawable.ic_check,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(AppSpacing.marginMobile)
                    .testTag("widget_config_save"),
        )
    }
}

@Composable
private fun PreviewArea(state: WidgetConfigState) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(AppShapes.xl)
                .background(AppThemeTokens.colors.fill)
                .padding(AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        WidgetPreviewCard(
            display = state.display,
            size = state.size,
            colors = widgetColorScheme(state.theme, state.background.effective(hasPhoto = state.photo != null)),
            photo = state.photo.takeIf { state.background == WidgetBackground.PHOTO },
            profiles = if (state.showProfile) state.profiles else emptyList(),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            DaysBadge(text = stringResource(state.size.label), tone = DaysBadgeTone.PRIVATE)
            Text(
                text = stringResource(R.string.widget_config_preview_hint),
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun ItemChoices(
    state: WidgetConfigState,
    onAction: (WidgetConfigAction) -> Unit,
) {
    DaysSection(title = stringResource(R.string.widget_config_item)) {
        ChoiceRow(
            title = stringResource(R.string.widget_config_item_auto),
            subtitle = stringResource(R.string.widget_config_item_auto_desc),
            selected = state.selectedItemId == null,
            onClick = { onAction(WidgetConfigAction.ItemSelected(null)) },
            leading = { DaysItemIcon(icon = ItemIcon.STAR, color = state.theme) },
            modifier = Modifier.testTag("widget_config_item_auto"),
        )
        state.choices.forEach { choice ->
            val content = choice.item.content
            ChoiceRow(
                title = content.title,
                subtitle = ddayLabel(choice.dday),
                selected = state.selectedItemId == choice.item.id,
                onClick = { onAction(WidgetConfigAction.ItemSelected(choice.item.id)) },
                leading = {
                    DaysItemIcon(
                        icon = ItemIcon.fromKey(content.iconKey),
                        color = itemColorFromKey(content.colorKey),
                    )
                },
                modifier = Modifier.testTag("widget_config_item_${choice.item.id}"),
            )
        }
    }
}

@Composable
private fun ChoiceRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    leading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    DaysListItem(
        title = title,
        subtitle = subtitle,
        leading = leading,
        modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        trailing = {
            if (selected) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_check),
                    contentDescription = stringResource(R.string.widget_config_item_selected),
                    tint = AppThemeTokens.colors.primary,
                )
            }
        },
    )
}

@Composable
private fun BackgroundChoices(
    state: WidgetConfigState,
    onAction: (WidgetConfigAction) -> Unit,
) {
    val colors = AppThemeTokens.colors
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            text = stringResource(R.string.widget_config_background),
            style = AppThemeTokens.typography.labelMd,
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = AppSpacing.xs),
        )
        if (state.background == WidgetBackground.PHOTO) {
            Text(
                text = stringResource(R.string.widget_config_background_photo_hint),
                style = AppThemeTokens.typography.bodySm,
                color = colors.textTertiary,
                modifier = Modifier.padding(horizontal = AppSpacing.xs),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            WidgetBackground.entries.forEach { background ->
                val selected = background == state.background
                val swatch = widgetColorScheme(state.theme, background).pick(LocalIsDarkTheme.current)
                DaysCard(
                    modifier =
                        Modifier
                            .weight(1f)
                            .then(if (selected) Modifier.border(2.dp, colors.primary, AppShapes.lg) else Modifier)
                            .selectable(selected = selected, role = Role.RadioButton) {
                                onAction(WidgetConfigAction.BackgroundSelected(background))
                            }.testTag("widget_config_background_${background.name.lowercase()}"),
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.sm),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(44.dp)
                                    .clip(AppShapes.md)
                                    .background(swatch.background)
                                    .border(1.dp, colors.border, AppShapes.md),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (background == WidgetBackground.PHOTO) {
                                DaysPhoto(
                                    file = state.photo,
                                    contentDescription = null,
                                    modifier = Modifier.size(44.dp),
                                )
                            }
                            Box(
                                modifier =
                                    Modifier
                                        .size(12.dp)
                                        .clip(AppShapes.full)
                                        .background(swatch.number),
                            )
                        }
                        Text(
                            text = stringResource(background.label),
                            style = AppThemeTokens.typography.labelMd,
                            color = if (selected) colors.primary else colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

private val WidgetBackground.label: Int
    get() =
        when (this) {
            WidgetBackground.THEME -> R.string.widget_config_background_theme
            WidgetBackground.DARK -> R.string.widget_config_background_dark
            WidgetBackground.WHITE -> R.string.widget_config_background_white
            WidgetBackground.PHOTO -> R.string.widget_config_background_photo
        }

@ScreenPreviews
@Composable
private fun WidgetConfigPreview() {
    AppTheme { WidgetConfigScreen(state = PreviewWidgetConfigState, onAction = {}) }
}
