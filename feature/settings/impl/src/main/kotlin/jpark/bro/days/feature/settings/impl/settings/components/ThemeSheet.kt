package jpark.bro.days.feature.settings.impl.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysBadge
import jpark.bro.days.core.designsystem.component.DaysBadgeTone
import jpark.bro.days.core.designsystem.component.DaysBottomSheet
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysPrimaryButton
import jpark.bro.days.core.designsystem.component.DaysSegment
import jpark.bro.days.core.designsystem.component.DaysSegmentedControl
import jpark.bro.days.core.designsystem.themePalette
import jpark.bro.days.core.model.AppThemeMode
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.feature.settings.impl.R
import jpark.bro.days.feature.settings.impl.settings.SettingsAction
import jpark.bro.days.feature.settings.impl.settings.ThemeSheetState
import jpark.bro.days.core.designsystem.R as DesignR

/** 테마 선택 바텀시트 (product-spec 6장, 시안 design/settings-theme-sheet) */
@Composable
internal fun ThemeSheet(
    sheet: ThemeSheetState,
    onAction: (SettingsAction) -> Unit,
) {
    DaysBottomSheet(onDismissRequest = { onAction(SettingsAction.ThemeSheetDismissed) }) {
        ThemeSheetContent(sheet = sheet, onAction = onAction)
    }
}

@Composable
internal fun ThemeSheetContent(
    sheet: ThemeSheetState,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = AppSpacing.lg)
                .padding(bottom = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.settings_theme_title),
                    style = typography.headlineMd,
                    color = colors.textPrimary,
                )
                Text(
                    stringResource(R.string.settings_theme_subtitle),
                    style = typography.bodySm,
                    color = colors.textSecondary,
                )
            }
            DaysIconButton(
                icon = DesignR.drawable.ic_close,
                contentDescription = stringResource(R.string.settings_theme_close),
                onClick = { onAction(SettingsAction.ThemeSheetDismissed) },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.settings_theme_color),
                    style = typography.headlineSm,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    stringResource(R.string.settings_theme_color_count),
                    style = typography.labelMd,
                    color = colors.primary,
                )
            }
            AppThemeType.entries.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.gutterCompact)) {
                    row.forEach { type ->
                        ThemeCard(
                            type = type,
                            selected = type == sheet.type,
                            onClick = { onAction(SettingsAction.ThemeTypeSelected(type)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text(
                stringResource(R.string.settings_theme_mode),
                style = typography.headlineSm,
                color = colors.textPrimary,
            )
            DaysSegmentedControl(
                segments =
                    AppThemeMode.entries.map { mode ->
                        when (mode) {
                            AppThemeMode.LIGHT -> {
                                DaysSegment(
                                    stringResource(R.string.settings_theme_mode_light),
                                    DesignR.drawable.ic_light_mode,
                                )
                            }

                            AppThemeMode.DARK -> {
                                DaysSegment(
                                    stringResource(R.string.settings_theme_mode_dark),
                                    DesignR.drawable.ic_dark_mode,
                                )
                            }

                            AppThemeMode.SYSTEM -> {
                                DaysSegment(
                                    stringResource(R.string.settings_theme_mode_system),
                                    DesignR.drawable.ic_brightness_auto,
                                )
                            }
                        }
                    },
                selectedIndex = sheet.mode.ordinal,
                onSelect = { onAction(SettingsAction.ThemeModeSelected(AppThemeMode.entries[it])) },
            )
            Text(
                stringResource(R.string.settings_theme_mode_hint),
                style = typography.bodySm,
                color = colors.textSecondary,
            )
        }

        DaysPrimaryButton(
            text = stringResource(R.string.settings_theme_apply),
            onClick = { onAction(SettingsAction.ThemeApplyClicked) },
            enabled = !sheet.isSaving,
            icon = DesignR.drawable.ic_palette,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** 카드 윗줄 높이 = 선택 체크 크기 (선택해도 카드 높이가 변하지 않게) */
private val CheckSize = 24.dp

/** 테마 카드: 태그 · 대표 색 3개(primary · accent · primary-soft) · 이름 · 설명 */
@Composable
private fun ThemeCard(
    type: AppThemeType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    val swatch = themePalette(type, dark = false)
    val label = type.label
    Column(
        modifier =
            modifier
                .clip(AppShapes.lg)
                .background(if (selected) colors.primarySoft else colors.surface)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) colors.primary else colors.border,
                    shape = AppShapes.lg,
                ).semantics { this.selected = selected }
                .clickable(role = Role.RadioButton, onClick = onClick)
                // border는 그리기만 하고 크기에 영향을 주지 않으므로 선택해도 내용 위치가 그대로다
                .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(CheckSize),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DaysBadge(text = stringResource(label.tag), tone = DaysBadgeTone.PRIVATE)
            Box(modifier = Modifier.weight(1f))
            if (selected) {
                Box(
                    modifier =
                        Modifier
                            .size(CheckSize)
                            .clip(AppShapes.full)
                            .background(colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_check),
                        contentDescription = stringResource(R.string.settings_theme_selected),
                        tint = colors.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs + 2.dp)) {
            listOf(swatch.primary, swatch.accent, swatch.primarySoft).forEach { color ->
                Box(
                    modifier =
                        Modifier
                            .size(20.dp)
                            .clip(AppShapes.full)
                            .background(color)
                            // primary-soft 점이 같은 색 카드 배경에서도 보이도록
                            .border(1.dp, colors.border, AppShapes.full),
                )
            }
        }
        Column {
            Text(stringResource(label.name), style = typography.labelLg, color = colors.textPrimary)
            Text(stringResource(label.description), style = typography.bodySm, color = colors.textSecondary)
        }
    }
}
