package jpark.bro.days.feature.home.impl.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysIconBadge
import jpark.bro.days.core.designsystem.component.DaysIconBadgeTone
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.feature.home.impl.R
import jpark.bro.days.core.designsystem.R as DesignR

/** 솔로일 때 "커플 코드로 연결하기" 카드 (product-spec 4-1, 닫기 가능) */
@Composable
internal fun CoupleBanner(
    onConnectClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DaysCard(modifier = modifier.testTag("home_couple_banner")) {
        Row(
            modifier = Modifier.padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            DaysIconBadge(icon = DesignR.drawable.ic_favorite, tone = DaysIconBadgeTone.SHARED)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Column {
                    Text(
                        text = stringResource(R.string.home_couple_banner_title),
                        style = AppThemeTokens.typography.labelLg,
                        color = AppThemeTokens.colors.textPrimary,
                    )
                    Text(
                        text = stringResource(R.string.home_couple_banner_desc),
                        style = AppThemeTokens.typography.bodySm,
                        color = AppThemeTokens.colors.textSecondary,
                    )
                }
                DaysSoftButton(
                    text = stringResource(R.string.home_couple_banner_connect),
                    onClick = onConnectClick,
                    size = DaysButtonSize.SMALL,
                    modifier = Modifier.testTag("home_couple_banner_connect"),
                )
            }
            DaysIconButton(
                icon = DesignR.drawable.ic_close,
                contentDescription = stringResource(R.string.home_couple_banner_close),
                onClick = onCloseClick,
                modifier = Modifier.testTag("home_couple_banner_close"),
            )
        }
    }
}
