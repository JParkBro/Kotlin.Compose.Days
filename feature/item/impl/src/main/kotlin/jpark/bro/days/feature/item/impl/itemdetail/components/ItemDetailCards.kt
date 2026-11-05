package jpark.bro.days.feature.item.impl.itemdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppFixedColors
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysBadge
import jpark.bro.days.core.designsystem.component.DaysBadgeTone
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysItemIcon
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.core.designsystem.component.DdayText
import jpark.bro.days.core.designsystem.component.DdayTone
import jpark.bro.days.core.designsystem.component.ddayLabel
import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.CountMode
import jpark.bro.days.core.model.DdayCount
import jpark.bro.days.core.model.Item
import jpark.bro.days.core.model.ItemIcon
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.model.RepeatRule
import jpark.bro.days.core.model.TimeSlot
import jpark.bro.days.core.model.itemColorFromKey
import jpark.bro.days.core.ui.DaysPhoto
import jpark.bro.days.core.ui.label
import jpark.bro.days.core.ui.scheduleText
import jpark.bro.days.core.ui.toDisplayText
import jpark.bro.days.feature.item.impl.R
import jpark.bro.days.feature.item.impl.itemdetail.DetailMilestone
import jpark.bro.days.feature.item.impl.itemedit.components.label
import java.io.File

/**
 * 위쪽 카드 (product-spec 4-4): 아이콘 · 뱃지 · 큰 D-day · 기준일 · 계산 방식 · 공개 범위.
 * 배경 사진이 있으면 사진 위에 어두운 그라데이션을 깔고 글자를 on-image(흰색)로 (theme-guide 4장)
 */
@Composable
internal fun DetailHeroCard(
    item: Item,
    dday: DdayCount?,
    modifier: Modifier = Modifier,
    photo: File? = null,
) {
    val colors = AppThemeTokens.colors
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = AppShapes.xl, ambientColor = colors.shadow, spotColor = colors.shadow)
                .clip(AppShapes.xl)
                .background(colors.primarySoft)
                .testTag("item_detail_hero"),
    ) {
        if (photo != null) {
            DaysPhoto(file = photo, contentDescription = null, modifier = Modifier.matchParentSize())
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(AppFixedColors.imageScrim.copy(alpha = 0f), AppFixedColors.imageScrim),
                            ),
                        ),
            )
        }
        HeroContent(item = item, dday = dday, onImage = photo != null)
    }
}

@Composable
private fun HeroContent(
    item: Item,
    dday: DdayCount?,
    onImage: Boolean,
) {
    val colors = AppThemeTokens.colors
    val content = item.content
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = if (onImage) PhotoHeroMinHeight else 0.dp)
                .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.Bottom),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            DaysItemIcon(icon = ItemIcon.fromKey(content.iconKey), color = itemColorFromKey(content.colorKey))
            if (content.type == ItemType.DDAY) {
                DaysBadge(text = stringResource(content.category.label), tone = DaysBadgeTone.PRIVATE)
            }
            if (content.repeatRule != RepeatRule.NONE) {
                DaysBadge(text = stringResource(content.repeatRule.label), tone = DaysBadgeTone.CATEGORY)
            }
        }
        Text(
            text = content.title,
            style = AppThemeTokens.typography.headlineSm,
            color = if (onImage) AppFixedColors.onImage else colors.onPrimarySoft,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (dday != null) {
            if (onImage) {
                Text(
                    text = ddayLabel(dday),
                    style = AppThemeTokens.typography.displayHeroMobile.copy(fontFeatureSettings = "tnum"),
                    color = AppFixedColors.onImage,
                )
            } else {
                DdayText(count = dday, tone = DdayTone.HIGHLIGHT, style = AppThemeTokens.typography.displayHeroMobile)
            }
        }
        Text(
            text = item.metaText(dday),
            style = AppThemeTokens.typography.bodySm,
            color = if (onImage) AppFixedColors.onImage else colors.textSecondary,
        )
    }
}

/** 사진이 보이도록 사진 카드는 조금 더 높게 */
private val PhotoHeroMinHeight = 240.dp

/**
 * 디데이: 2026.08.07 (금)부터 · D+ 방식 · 1일 기준 · 나만 보기
 * 일정: 2026.11.06 (금) · 저녁 · 성수동 · 나만 보기
 */
@Composable
private fun Item.metaText(dday: DdayCount?): String {
    val date = content.date?.toDisplayText()
    val parts =
        when (content.type) {
            ItemType.DDAY -> {
                listOf(
                    if (date != null &&
                        dday is DdayCount.Elapsed
                    ) {
                        stringResource(R.string.item_detail_since, date)
                    } else {
                        date
                    },
                    stringResource(
                        if (content.countMode ==
                            CountMode.DPLUS
                        ) {
                            R.string.item_detail_count_dplus
                        } else {
                            R.string.item_detail_count_dday
                        },
                    ),
                    stringResource(R.string.item_detail_count_first_day).takeIf {
                        content.countMode == CountMode.DPLUS && content.countFirstDayAsOne
                    },
                )
            }

            ItemType.PLAN -> {
                listOf(content.scheduleText(showPlace = true).ifEmpty { null })
            }
        }
    val visibility = stringResource(content.visibility.label)
    return (parts + visibility).filterNotNull().joinToString(" · ")
}

/** 다가오는 기념일 (product-spec 4-4): 지난 것은 흐리게, 다음 것은 강조 (theme-guide 5.5) */
@Composable
internal fun MilestoneTimeline(
    milestones: List<DetailMilestone>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.testTag("item_detail_milestones"),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.item_detail_milestones),
            style = AppThemeTokens.typography.headlineSm,
            color = AppThemeTokens.colors.textPrimary,
            modifier = Modifier.padding(horizontal = AppSpacing.xs),
        )
        milestones.forEach { MilestoneRow(it) }
    }
}

@Composable
private fun MilestoneRow(milestone: DetailMilestone) {
    val colors = AppThemeTokens.colors
    val textColor = if (milestone.isPassed) colors.textTertiary else colors.textPrimary
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(AppShapes.lg)
                .background(if (milestone.isNext) colors.primarySoft else colors.surface)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = milestone.anniversary.name(),
                    style = AppThemeTokens.typography.labelLg,
                    color = if (milestone.isNext) colors.onPrimarySoft else textColor,
                )
                if (milestone.isNext) {
                    DaysBadge(text = stringResource(R.string.item_detail_milestone_next), tone = DaysBadgeTone.SHARED)
                }
            }
            Text(
                text = milestone.anniversary.date.toDisplayText(),
                style = AppThemeTokens.typography.bodySm,
                color = if (milestone.isPassed) colors.textTertiary else colors.textSecondary,
            )
        }
        if (milestone.isPassed) {
            DaysBadge(text = stringResource(R.string.item_detail_milestone_passed), tone = DaysBadgeTone.PRIVATE)
        } else {
            DdayText(count = milestone.dday, tone = if (milestone.isNext) DdayTone.HIGHLIGHT else DdayTone.DEFAULT)
        }
    }
}

/** 600일 · 2주년 (상세 타임라인에는 생일이 없다) */
@Composable
private fun AutoAnniversary.name(): String =
    when (this) {
        is AutoAnniversary.Days -> stringResource(R.string.item_detail_milestone_days, days)
        is AutoAnniversary.Years -> stringResource(R.string.item_detail_milestone_years, years)
        is AutoAnniversary.Birthday -> date.toDisplayText()
    }

/** 메모 카드: 수정을 누르면 수정 화면 */
@Composable
internal fun MemoCard(
    memo: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DaysCard(modifier = modifier.testTag("item_detail_memo")) {
        Column(modifier = Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.item_detail_memo),
                    style = AppThemeTokens.typography.headlineSm,
                    color = AppThemeTokens.colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                DaysSoftButton(
                    text = stringResource(R.string.item_detail_edit),
                    onClick = onEditClick,
                    size = DaysButtonSize.SMALL,
                )
            }
            Text(text = memo, style = AppThemeTokens.typography.bodyMd, color = AppThemeTokens.colors.textPrimary)
        }
    }
}
