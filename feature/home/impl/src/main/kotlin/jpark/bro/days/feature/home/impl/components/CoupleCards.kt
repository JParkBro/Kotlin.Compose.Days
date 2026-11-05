package jpark.bro.days.feature.home.impl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
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
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysFilterChip
import jpark.bro.days.core.designsystem.component.DaysProgressBar
import jpark.bro.days.core.designsystem.component.DdayText
import jpark.bro.days.core.designsystem.component.DdayTone
import jpark.bro.days.core.domain.anniversary.NextAnniversary
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.ui.ProfileAvatar
import jpark.bro.days.core.ui.scheduleText
import jpark.bro.days.core.ui.title
import jpark.bro.days.core.ui.toDisplayText
import jpark.bro.days.feature.home.impl.CoupleSummary
import jpark.bro.days.feature.home.impl.HomeFilter
import jpark.bro.days.feature.home.impl.R
import jpark.bro.days.core.designsystem.R as DesignR

/** 커플 히어로 (product-spec 4-1): 두 사람 + "함께한 지 N일". 사진은 이미지 저장소 단계(6-8)에서 */
@Composable
internal fun CoupleHeroCard(
    couple: CoupleSummary,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = AppShapes.xl, ambientColor = colors.shadow, spotColor = colors.shadow)
                .clip(AppShapes.xl)
                .background(colors.primarySoft)
                .padding(AppSpacing.lg)
                .testTag("home_couple_hero"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        CoupleAvatars(couple = couple)
        Text(
            text =
                stringResource(
                    R.string.home_couple_together,
                    couple.partnerName ?: stringResource(R.string.home_couple_partner_unknown),
                    couple.myName ?: stringResource(R.string.home_couple_me_unknown),
                ),
            style = AppThemeTokens.typography.labelLg,
            color = colors.onPrimarySoft,
            modifier = Modifier.padding(top = AppSpacing.sm),
        )
        Text(
            text = stringResource(R.string.home_couple_days, couple.togetherDays),
            style = AppThemeTokens.typography.displayHeroMobile.copy(fontFeatureSettings = "tnum"),
            color = colors.primary,
            modifier = Modifier.testTag("home_couple_days"),
        )
        Text(
            text = stringResource(R.string.home_couple_since, couple.startDate.toDisplayText()),
            style = AppThemeTokens.typography.bodyMd,
            color = colors.textSecondary,
        )
    }
}

/** 두 사람 사진(없으면 이름 첫 글자) + 가운데 하트 */
@Composable
private fun CoupleAvatars(couple: CoupleSummary) {
    val colors = AppThemeTokens.colors
    Box(contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy((-AvatarOverlap))) {
            ProfileAvatar(file = couple.partnerPhoto, name = couple.partnerName, size = AvatarSize)
            ProfileAvatar(file = couple.myPhoto, name = couple.myName, size = AvatarSize)
        }
        Box(
            modifier =
                Modifier
                    .size(28.dp)
                    .clip(AppShapes.full)
                    .background(colors.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_favorite),
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/** 커플 홈 2열: 다음 일정 | 다음 기념일 (product-spec 4-1) */
@Composable
internal fun CoupleSummaryRow(
    plan: Item?,
    couple: CoupleSummary,
    onPlanClick: (Item) -> Unit,
    onAddPlanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        val cardModifier = Modifier.weight(1f).fillMaxHeight()
        NextPlanSmallCard(
            plan = plan,
            onClick = { if (plan != null) onPlanClick(plan) else onAddPlanClick() },
            modifier = cardModifier,
        )
        NextAnniversaryCard(
            next = couple.nextAnniversary,
            nicknames = couple.nicknames,
            modifier = cardModifier,
        )
    }
}

@Composable
private fun NextPlanSmallCard(
    plan: Item?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    DaysCard(modifier = modifier.testTag("home_next_plan"), onClick = onClick) {
        SmallCardBody(icon = DesignR.drawable.ic_calendar, label = stringResource(R.string.home_next_plan)) {
            Text(
                text = plan?.content?.title ?: stringResource(R.string.home_next_plan_add),
                style = AppThemeTokens.typography.headlineSm,
                color = if (plan == null) colors.primary else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = plan?.content?.scheduleText()?.ifEmpty { null } ?: stringResource(R.string.home_next_plan_empty),
                style = AppThemeTokens.typography.bodySm,
                color = colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun NextAnniversaryCard(
    next: NextAnniversary?,
    nicknames: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    DaysCard(modifier = modifier.testTag("home_next_anniversary")) {
        SmallCardBody(
            icon = DesignR.drawable.ic_redeem,
            label = stringResource(R.string.home_next_anniversary),
            trailing =
                next?.let {
                    { DdayText(count = it.dday, tone = DdayTone.HIGHLIGHT, style = AppThemeTokens.typography.labelMd) }
                },
        ) {
            if (next == null) {
                Text(
                    text = stringResource(R.string.home_next_anniversary_empty),
                    style = AppThemeTokens.typography.bodySm,
                    color = colors.textSecondary,
                )
            } else {
                Text(
                    text = next.anniversary.title(nicknames::get),
                    style = AppThemeTokens.typography.headlineSm,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = next.anniversary.date.toDisplayText(),
                    style = AppThemeTokens.typography.bodySm,
                    color = colors.textSecondary,
                )
                DaysProgressBar(
                    progress = next.progress,
                    height = 4.dp,
                    modifier = Modifier.padding(top = AppSpacing.xs),
                )
            }
        }
    }
}

/** 작은 카드 공통: 아이콘 + 라벨 (+ 오른쪽 표시) 다음에 내용 */
@Composable
private fun SmallCardBody(
    icon: Int,
    label: String,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = AppThemeTokens.colors
    Column(
        modifier = Modifier.padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = AppThemeTokens.typography.labelMd,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            trailing?.invoke()
        }
        content()
    }
}

/** 공개 범위 필터 (커플만): 전체 / 우리 / 나만 */
@Composable
internal fun HomeFilterChips(
    selected: HomeFilter,
    onSelect: (HomeFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        HomeFilter.entries.forEach { filter ->
            DaysFilterChip(
                text =
                    stringResource(
                        when (filter) {
                            HomeFilter.ALL -> R.string.home_filter_all
                            HomeFilter.SHARED -> R.string.home_filter_shared
                            HomeFilter.PRIVATE -> R.string.home_filter_private
                        },
                    ),
                selected = filter == selected,
                onClick = { onSelect(filter) },
                modifier = Modifier.testTag("home_filter_${filter.name.lowercase()}"),
            )
        }
    }
}

private val AvatarSize = 64.dp
private val AvatarOverlap = 12.dp
