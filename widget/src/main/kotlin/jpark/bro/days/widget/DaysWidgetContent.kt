package jpark.bro.days.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import jpark.bro.days.core.designsystem.AppRadius
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.DefaultTypography
import jpark.bro.days.core.designsystem.component.iconRes
import jpark.bro.days.core.domain.dday.ItemDday
import jpark.bro.days.core.model.AppThemeType
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemContent
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.WidgetBackground
import jpark.bro.days.core.model.WidgetSize
import jpark.bro.days.core.navigation.AppIntents
import jpark.bro.days.core.ui.toDisplayText
import java.time.LocalDate
import jpark.bro.days.core.designsystem.R as DesignR

/** 위젯 화면: 작게는 아이콘 · 제목 · D-day, 중간은 기준일까지 (product-spec 8장) */
@Composable
internal fun DaysWidgetContent(
    display: WidgetDisplay,
    size: WidgetSize,
    colors: WidgetColors,
) {
    val context = LocalContext.current
    Column(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .cornerRadius(AppRadius.xl)
                .background(colors.background)
                .padding(AppSpacing.md)
                .clickable(
                    actionStartActivity(context.openAppIntent((display as? WidgetDisplay.Dday)?.entry?.item?.id)),
                ),
        verticalAlignment = Alignment.Vertical.Top,
    ) {
        when (display) {
            is WidgetDisplay.Dday -> DdayBody(entry = display.entry, size = size, colors = colors)
            WidgetDisplay.Empty -> MessageBody(text = context.getString(R.string.widget_empty), colors = colors)
            WidgetDisplay.Missing -> MessageBody(text = context.getString(R.string.widget_missing), colors = colors)
        }
    }
}

@Composable
private fun ColumnScope.DdayBody(
    entry: ItemDday,
    size: WidgetSize,
    colors: WidgetColors,
) {
    val context = LocalContext.current
    val content = entry.item.content
    val typography = DefaultTypography
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            provider = ImageProvider(ItemIcon.fromKey(content.iconKey).iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colors.number),
            modifier = GlanceModifier.size(IconSize),
        )
        Spacer(GlanceModifier.width(AppSpacing.xs))
        Text(
            text = content.title,
            style =
                TextStyle(
                    color = colors.title,
                    fontSize = typography.labelLg.fontSize,
                    fontWeight = FontWeight.Medium,
                ),
            maxLines = 1,
        )
    }
    Spacer(GlanceModifier.defaultWeight())
    Text(
        text = context.ddayText(entry.dday),
        style =
            TextStyle(
                color = colors.number,
                fontSize = typography.displayHeroMobile.fontSize,
                fontWeight = FontWeight.Bold,
            ),
        maxLines = 1,
    )
    if (size == WidgetSize.MEDIUM) {
        content.date?.let { date ->
            Text(
                text = date.toDisplayText(),
                style = TextStyle(color = colors.secondary, fontSize = typography.bodySm.fontSize),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ColumnScope.MessageBody(
    text: String,
    colors: WidgetColors,
) {
    Spacer(GlanceModifier.defaultWeight())
    Text(text = text, style = TextStyle(color = colors.title, fontSize = DefaultTypography.bodyMd.fontSize))
    Spacer(GlanceModifier.defaultWeight())
}

private val IconSize = 20.dp

private fun Context.ddayText(dday: DdayCount): String =
    when (dday) {
        is DdayCount.Remaining -> getString(DesignR.string.common_dday_remaining, dday.days)
        DdayCount.Today -> getString(DesignR.string.common_dday_today)
        is DdayCount.Elapsed -> getString(DesignR.string.common_dday_elapsed, dday.days)
    }

/** 앱을 연다. [itemId]가 있으면 [홈, 상세] 백스택으로 (MainActivity가 처리) */
internal fun Context.openAppIntent(itemId: String?): Intent =
    (packageManager.getLaunchIntentForPackage(packageName) ?: Intent()).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        if (itemId != null) putExtra(AppIntents.EXTRA_OPEN_ITEM_ID, itemId)
    }

/** product-spec 9장 샘플: 전역일 D-134 */
@Suppress("MagicNumber") // 샘플 데이터
private val PreviewEntry =
    ItemDday(
        item =
            Item(
                id = "discharge",
                content =
                    ItemContent(
                        type = ItemType.DDAY,
                        title = "전역일",
                        colorKey = AppThemeType.PINK.name,
                        iconKey = ItemIcon.FLAG.name,
                        date = LocalDate.of(2027, 3, 19),
                    ),
                ownerUserId = "user-me",
                updatedBy = "user-me",
                createdAt = 0L,
                updatedAt = 0L,
            ),
        dday = DdayCount.Remaining(134),
    )

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 110, heightDp = 110)
@Composable
private fun SmallWidgetPreview() {
    DaysWidgetContent(
        WidgetDisplay.Dday(PreviewEntry),
        WidgetSize.SMALL,
        widgetColors(AppThemeType.PINK, WidgetBackground.THEME),
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 110)
@Composable
private fun MediumWidgetPreview() {
    DaysWidgetContent(
        WidgetDisplay.Dday(PreviewEntry),
        WidgetSize.MEDIUM,
        widgetColors(AppThemeType.NAVY, WidgetBackground.DARK),
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 110, heightDp = 110)
@Composable
private fun EmptyWidgetPreview() {
    DaysWidgetContent(WidgetDisplay.Empty, WidgetSize.SMALL, widgetColors(AppThemeType.SAGE, WidgetBackground.WHITE))
}
