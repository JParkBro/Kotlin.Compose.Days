package jpark.bro.days.feature.home.impl.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysPrimaryButton
import jpark.bro.days.core.designsystem.component.DaysSecondaryButton
import jpark.bro.days.feature.home.impl.QuickStart
import jpark.bro.days.feature.home.impl.R
import jpark.bro.days.core.designsystem.R as DesignR

/** 빈 상태 (product-spec 4-1): 일러스트 · 안내 문구 · 빠른 시작 칩 · 추가 버튼 */
@Composable
internal fun HomeEmptyContent(
    onQuickStartClick: (QuickStart) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        EmptyIllustration()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.home_empty_title),
                style = typography.headlineMd,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.home_empty_subtitle),
                style = typography.bodyMd,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.home_empty_quick_start),
                style = typography.labelMd,
                color = colors.textSecondary,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                QuickStart.entries.forEach { quickStart ->
                    DaysSecondaryButton(
                        text = stringResource(quickStart.label),
                        onClick = { onQuickStartClick(quickStart) },
                        size = DaysButtonSize.SMALL,
                        icon = quickStart.icon,
                        modifier = Modifier.testTag("home_empty_quick_${quickStart.name.lowercase()}"),
                    )
                }
            }
        }
        DaysPrimaryButton(
            text = stringResource(R.string.home_empty_add),
            onClick = onAddClick,
            icon = DesignR.drawable.ic_add,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("home_empty_add"),
        )
    }
}

/** 일러스트: primary-soft 원 안의 달력 아이콘 */
@Composable
private fun EmptyIllustration() {
    Box(
        modifier =
            Modifier
                .size(160.dp)
                .clip(AppShapes.full)
                .background(AppThemeTokens.colors.primarySoft),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_calendar),
            contentDescription = null,
            tint = AppThemeTokens.colors.primary,
            modifier = Modifier.size(72.dp),
        )
    }
}

@get:StringRes
internal val QuickStart.label: Int
    get() =
        when (this) {
            QuickStart.BIRTHDAY -> R.string.home_empty_quick_birthday
            QuickStart.TRAVEL -> R.string.home_empty_quick_travel
            QuickStart.EXAM -> R.string.home_empty_quick_exam
            QuickStart.DISCHARGE -> R.string.home_empty_quick_discharge
        }

@get:DrawableRes
private val QuickStart.icon: Int
    get() =
        when (this) {
            QuickStart.BIRTHDAY -> DesignR.drawable.ic_cake
            QuickStart.TRAVEL -> DesignR.drawable.ic_flight
            QuickStart.EXAM -> DesignR.drawable.ic_school
            QuickStart.DISCHARGE -> DesignR.drawable.ic_flag
        }
