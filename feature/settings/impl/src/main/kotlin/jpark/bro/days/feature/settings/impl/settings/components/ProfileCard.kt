package jpark.bro.days.feature.settings.impl.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysBadge
import jpark.bro.days.core.designsystem.component.DaysBadgeTone
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.feature.settings.impl.R
import jpark.bro.days.feature.settings.impl.settings.ProfileSummary
import java.time.format.DateTimeFormatter
import jpark.bro.days.core.designsystem.R as DesignR

private val BirthDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

@Composable
internal fun ProfileCard(
    profile: ProfileSummary?,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DaysCard(modifier = modifier) {
        ProfileCardContent(profile = profile, onEditClick = onEditClick)
    }
}

@Composable
private fun ProfileCardContent(
    profile: ProfileSummary?,
    onEditClick: () -> Unit,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(AppShapes.full)
                    .background(colors.fill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_person),
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(28.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = profile?.nickname ?: stringResource(R.string.settings_main_profile_empty),
                    style = if (profile != null) typography.headlineSm else typography.labelLg,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                profile?.togetherDays?.let { days ->
                    DaysBadge(
                        text = stringResource(R.string.settings_main_profile_days, days),
                        tone = DaysBadgeTone.SHARED,
                    )
                }
            }
            profile?.birthDate?.let { birthDate ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_cake),
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = birthDate.format(BirthDateFormat),
                        style = typography.bodySm,
                        color = colors.textSecondary,
                    )
                }
            }
        }
        DaysSoftButton(
            text = stringResource(R.string.settings_main_profile_edit),
            onClick = onEditClick,
            size = DaysButtonSize.SMALL,
        )
    }
}
