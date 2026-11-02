package jpark.bro.days.core.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysBottomSheet
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysIconBadge
import jpark.bro.days.core.designsystem.component.DaysIconBadgeTone
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.designsystem.R as DesignR

/** + 버튼 시트: [디데이 / 일정] 선택 (product-spec 4-2, 시안 design/home-add-sheet). 홈 · 캘린더 공용 */
@Composable
fun AddItemSheet(
    onSelect: (ItemType) -> Unit,
    onDismiss: () -> Unit,
) {
    DaysBottomSheet(onDismissRequest = onDismiss) {
        AddItemSheetContent(onSelect = onSelect, onClose = onDismiss)
    }
}

@Composable
fun AddItemSheetContent(
    onSelect: (ItemType) -> Unit,
    onClose: () -> Unit,
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.common_add_sheet_title),
                style = AppThemeTokens.typography.headlineMd,
                color = AppThemeTokens.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            DaysIconButton(
                icon = DesignR.drawable.ic_close,
                contentDescription = stringResource(R.string.common_add_sheet_close),
                onClick = onClose,
            )
        }
        AddOption(
            title = stringResource(R.string.common_add_sheet_dday),
            description = stringResource(R.string.common_add_sheet_dday_desc),
            icon = DesignR.drawable.ic_favorite,
            tone = DaysIconBadgeTone.SHARED,
            onClick = { onSelect(ItemType.DDAY) },
            modifier = Modifier.testTag("add_sheet_dday"),
        )
        AddOption(
            title = stringResource(R.string.common_add_sheet_plan),
            description = stringResource(R.string.common_add_sheet_plan_desc),
            icon = DesignR.drawable.ic_event,
            tone = DaysIconBadgeTone.REPEAT,
            onClick = { onSelect(ItemType.PLAN) },
            modifier = Modifier.testTag("add_sheet_plan"),
        )
    }
}

@Composable
private fun AddOption(
    title: String,
    description: String,
    @DrawableRes icon: Int,
    tone: DaysIconBadgeTone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DaysCard(modifier = modifier, onClick = onClick) {
        DaysListItem(
            title = title,
            subtitle = description,
            showChevron = true,
            leading = { DaysIconBadge(icon = icon, tone = tone) },
        )
    }
}

@ComponentPreviews
@Composable
private fun AddItemSheetPreview() {
    AppTheme {
        Box(modifier = Modifier.background(AppThemeTokens.colors.surfaceRaised)) {
            AddItemSheetContent(onSelect = {}, onClose = {})
        }
    }
}
