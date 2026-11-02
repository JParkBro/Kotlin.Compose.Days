package jpark.bro.days.feature.home.impl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
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
import jpark.bro.days.core.designsystem.component.DaysIconBadge
import jpark.bro.days.core.designsystem.component.DaysItemIcon
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.core.designsystem.component.DdayText
import jpark.bro.days.core.designsystem.component.DdayTone
import jpark.bro.days.core.domain.dday.isImminent
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import jpark.bro.days.core.model.itemColorFromKey
import jpark.bro.days.core.ui.label
import jpark.bro.days.core.ui.toDisplayText
import jpark.bro.days.feature.home.impl.HomeHero
import jpark.bro.days.feature.home.impl.HomeRow
import jpark.bro.days.feature.home.impl.R
import jpark.bro.days.core.designsystem.R as DesignR

/** 히어로 (product-spec 4-1): primary-soft 카드 + 큰 D-day (theme-guide 6장) */
@Composable
internal fun HeroCard(
    hero: HomeHero,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val content = hero.row.item.content
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = AppShapes.xl, ambientColor = colors.shadow, spotColor = colors.shadow)
                .clip(AppShapes.xl)
                .background(colors.primarySoft)
                .clickable(onClick = onClick)
                .padding(AppSpacing.lg)
                .testTag("home_hero"),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DaysBadge(
            text = stringResource(if (hero.isPinned) R.string.home_hero_pinned else R.string.home_hero_nearest),
            tone = DaysBadgeTone.PRIVATE,
        )
        Text(
            text = content.title,
            style = AppThemeTokens.typography.headlineSm,
            color = colors.onPrimarySoft,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        DdayText(
            count = hero.row.dday,
            tone = DdayTone.HIGHLIGHT,
            style = AppThemeTokens.typography.displayHeroMobile,
        )
        content.date?.let { date ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_calendar),
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = date.toDisplayText(),
                    style = AppThemeTokens.typography.bodySm,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

/** 다가오는 일정 1개, 없으면 "일정 추가" (product-spec 4-1) */
@Composable
internal fun NextPlanCard(
    plan: Item?,
    onPlanClick: (Item) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DaysCard(
        modifier = modifier.testTag("home_next_plan"),
        onClick = plan?.let { { onPlanClick(it) } },
    ) {
        DaysListItem(
            title = plan?.content?.title ?: stringResource(R.string.home_next_plan),
            subtitle = plan?.scheduleText() ?: stringResource(R.string.home_next_plan_empty),
            leading = { DaysIconBadge(icon = DesignR.drawable.ic_calendar) },
            trailing =
                if (plan == null) {
                    {
                        DaysSoftButton(
                            text = stringResource(R.string.home_next_plan_add),
                            onClick = onAddClick,
                            size = DaysButtonSize.SMALL,
                            icon = DesignR.drawable.ic_add,
                            modifier = Modifier.testTag("home_next_plan_add"),
                        )
                    }
                } else {
                    null
                },
        )
    }
}

/** 2026.11.06 (금) · 저녁 / 2026.11.06 (금) · 오후 07:00 */
@Composable
private fun Item.scheduleText(): String {
    val date = content.date?.toDisplayText().orEmpty()
    val slot = content.timeSlot ?: return date
    val time = if (slot == TimeSlot.EXACT) content.time?.toDisplayText() else stringResource(slot.label)
    return listOfNotNull(date, time).joinToString(" · ")
}

/** 디데이 리스트 한 줄. 숫자는 7일 이내면 danger, 나머지는 text-primary (theme-guide 5.5) */
@Composable
internal fun HomeItemRow(
    row: HomeRow,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = row.item.content
    val date = content.date?.toDisplayText().orEmpty()
    DaysCard(modifier = modifier.testTag("home_item_${row.item.id}"), onClick = onClick) {
        DaysListItem(
            title = content.title,
            subtitle = if (row.dday is DdayCount.Elapsed) stringResource(R.string.home_list_since, date) else date,
            leading = {
                DaysItemIcon(icon = ItemIcon.fromKey(content.iconKey), color = itemColorFromKey(content.colorKey))
            },
            trailing = {
                if (content.repeatRule != RepeatRule.NONE) {
                    DaysBadge(text = stringResource(content.repeatRule.label), tone = DaysBadgeTone.CATEGORY)
                }
                DdayText(count = row.dday, tone = if (row.dday.isImminent()) DdayTone.SOON else DdayTone.DEFAULT)
            },
        )
    }
}

/** 리스트 제목 + 개수 */
@Composable
internal fun HomeListHeader(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_list_title),
            style = AppThemeTokens.typography.headlineSm,
            color = AppThemeTokens.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.home_list_count, count),
            style = AppThemeTokens.typography.labelMd,
            color = AppThemeTokens.colors.textSecondary,
        )
    }
}
