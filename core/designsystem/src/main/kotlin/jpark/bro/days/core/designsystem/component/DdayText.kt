package jpark.bro.days.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.R
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.designsystem.preview.DaysPreviewSurface
import jpark.bro.days.core.model.DdayCount

/** D-N · D-Day · D+N. 숫자 폭이 흔들리지 않게 tabular-nums를 켠다 */
@Composable
fun DdayText(
    count: DdayCount,
    modifier: Modifier = Modifier,
    tone: DdayTone = DdayTone.DEFAULT,
    style: TextStyle = AppThemeTokens.typography.metricTabular,
) {
    val colors = AppThemeTokens.colors
    Text(
        text = ddayLabel(count),
        style = style.copy(fontFeatureSettings = "tnum"),
        color =
            when (tone) {
                DdayTone.HIGHLIGHT -> colors.primary
                DdayTone.DEFAULT -> colors.textPrimary
                DdayTone.SOON -> colors.danger
                DdayTone.PASSED -> colors.textTertiary
            },
        modifier = modifier,
    )
}

/** D-N · D-Day · D+N 문구 (색 없이 글자만 필요할 때) */
@Composable
fun ddayLabel(count: DdayCount): String =
    when (count) {
        is DdayCount.Remaining -> stringResource(R.string.common_dday_remaining, count.days)
        DdayCount.Today -> stringResource(R.string.common_dday_today)
        is DdayCount.Elapsed -> stringResource(R.string.common_dday_elapsed, count.days)
    }

/** product-spec 9장 샘플 (오늘 = 2026.11.05) */
@ComponentPreviews
@Composable
private fun DdayTextPreview() {
    DaysPreviewSurface {
        DdayText(
            count = DdayCount.Remaining(134),
            tone = DdayTone.HIGHLIGHT,
            style = AppThemeTokens.typography.displayHeroMobile,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            DdayText(count = DdayCount.Remaining(58))
            DdayText(count = DdayCount.Remaining(1), tone = DdayTone.SOON)
            DdayText(count = DdayCount.Today, tone = DdayTone.SOON)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            DdayText(count = DdayCount.Elapsed(91))
            DdayText(count = DdayCount.Elapsed(500), tone = DdayTone.PASSED)
        }
    }
}
