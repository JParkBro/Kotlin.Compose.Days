package jpark.bro.days.core.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysBottomSheet
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysIconBadge
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.model.WidgetSize
import jpark.bro.days.core.designsystem.R as DesignR

/** 위젯 크기 이름 */
@get:StringRes
val WidgetSize.label: Int
    get() =
        when (this) {
            WidgetSize.SMALL -> R.string.common_widget_size_small
            WidgetSize.MEDIUM -> R.string.common_widget_size_medium
        }

/** 위젯 추가 크기 고르기 (상세 "위젯 추가" · 위젯 설정 "위젯 추가" 공용) */
@Composable
fun WidgetSizeSheet(
    onSelect: (WidgetSize) -> Unit,
    onDismiss: () -> Unit,
) {
    DaysBottomSheet(onDismissRequest = onDismiss) {
        WidgetSizeSheetContent(onSelect = onSelect)
    }
}

@Composable
fun WidgetSizeSheetContent(
    onSelect: (WidgetSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .navigationBarsPadding()
                .padding(horizontal = AppSpacing.lg)
                .padding(bottom = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Column {
            Text(
                text = stringResource(R.string.common_widget_size_sheet_title),
                style = AppThemeTokens.typography.headlineMd,
                color = AppThemeTokens.colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.common_widget_size_sheet_desc),
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textSecondary,
            )
        }
        WidgetSize.entries.forEach { size ->
            DaysCard(
                onClick = { onSelect(size) },
                modifier = Modifier.testTag("widget_size_${size.name.lowercase()}"),
            ) {
                DaysListItem(
                    title = stringResource(size.label),
                    showChevron = true,
                    leading = { DaysIconBadge(icon = DesignR.drawable.ic_widgets) },
                )
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun WidgetSizeSheetPreview() {
    AppTheme {
        Box(modifier = Modifier.background(AppThemeTokens.colors.surfaceRaised)) {
            WidgetSizeSheetContent(onSelect = {})
        }
    }
}
