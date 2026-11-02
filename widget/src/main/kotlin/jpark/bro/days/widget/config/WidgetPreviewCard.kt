package jpark.bro.days.widget.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.LocalIsDarkTheme
import jpark.bro.days.core.designsystem.component.ddayLabel
import jpark.bro.days.core.designsystem.component.iconRes
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.WidgetSize
import jpark.bro.days.core.ui.toDisplayText
import jpark.bro.days.widget.R
import jpark.bro.days.widget.WidgetColorScheme
import jpark.bro.days.widget.WidgetDisplay

/** 위젯 미리보기 (Compose). 위젯(Glance)과 같은 색 · 배치 */
@Composable
internal fun WidgetPreviewCard(
    display: WidgetDisplay,
    size: WidgetSize,
    colors: WidgetColorScheme,
    modifier: Modifier = Modifier,
) {
    val palette = colors.pick(LocalIsDarkTheme.current)
    val typography = AppThemeTokens.typography
    Column(
        modifier =
            modifier
                .size(width = if (size == WidgetSize.SMALL) SmallWidth else MediumWidth, height = WidgetHeight)
                .shadow(elevation = 8.dp, shape = AppShapes.xl, ambientColor = AppThemeTokens.colors.shadow)
                .clip(AppShapes.xl)
                .background(palette.background)
                .padding(AppSpacing.md)
                .testTag("widget_config_preview"),
    ) {
        when (display) {
            is WidgetDisplay.Dday -> {
                val content = display.entry.item.content
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Icon(
                        painter = painterResource(ItemIcon.fromKey(content.iconKey).iconRes),
                        contentDescription = null,
                        tint = palette.number,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = content.title,
                        style = typography.labelLg,
                        color = palette.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = ddayLabel(display.entry.dday),
                    style = typography.displayHeroMobile,
                    color = palette.number,
                    maxLines = 1,
                )
                if (size == WidgetSize.MEDIUM) {
                    content.date?.let {
                        Text(
                            text = it.toDisplayText(),
                            style = typography.bodySm,
                            color = palette.secondary,
                        )
                    }
                }
            }

            WidgetDisplay.Empty, WidgetDisplay.Missing -> {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text =
                        stringResource(
                            if (display ==
                                WidgetDisplay.Empty
                            ) {
                                R.string.widget_empty
                            } else {
                                R.string.widget_missing
                            },
                        ),
                    style = typography.bodyMd,
                    color = palette.title,
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/** 실제 2×2 · 4×2 크기와 비슷한 비율 */
private val SmallWidth = 160.dp
private val MediumWidth = 320.dp
private val WidgetHeight = 160.dp
