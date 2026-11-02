package jpark.bro.days.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppColors
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.designsystem.preview.DaysPreviewSurface

/** 뱃지 종류 (theme-guide.md 6장). 색만으로 구분하지 않도록 항상 문구와 함께 쓴다 (5.8) */
enum class DaysBadgeTone {
    /** 우리 둘이: primary-soft */
    SHARED,

    /** 나만 보기: fill */
    PRIVATE,

    /** 매년 · 카테고리: accent-soft */
    CATEGORY,

    /** 곧 도래: danger-soft */
    SOON,

    /** 연결됨 · 완료: success-soft */
    SUCCESS,
}

enum class DaysBadgeSize {
    /** 리스트 · 카드 안 (labelSm) */
    SMALL,

    /** 설정 행 끝 상태 표시처럼 조금 큰 것 (labelMd) */
    MEDIUM,
}

/** 알약 모양 뱃지. [showDot]이면 앞에 상태 점을 붙인다 */
@Composable
fun DaysBadge(
    text: String,
    tone: DaysBadgeTone,
    modifier: Modifier = Modifier,
    size: DaysBadgeSize = DaysBadgeSize.SMALL,
    showDot: Boolean = false,
) {
    val colors = AppThemeTokens.colors.badgeColors(tone)
    val typography = AppThemeTokens.typography
    Row(
        modifier =
            modifier
                .clip(AppShapes.full)
                .background(colors.background)
                .then(
                    when (size) {
                        DaysBadgeSize.SMALL -> Modifier.padding(horizontal = AppSpacing.sm, vertical = 2.dp)
                        DaysBadgeSize.MEDIUM -> Modifier.padding(horizontal = 12.dp, vertical = AppSpacing.xs)
                    },
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (showDot) {
            Box(
                modifier =
                    Modifier
                        .size(if (size == DaysBadgeSize.SMALL) 6.dp else 8.dp)
                        .clip(AppShapes.full)
                        .background(colors.dot),
            )
        }
        Text(
            text = text,
            style = if (size == DaysBadgeSize.SMALL) typography.labelSm else typography.labelMd,
            color = colors.content,
        )
    }
}

private data class BadgeColors(
    val background: Color,
    val content: Color,
    val dot: Color,
)

private fun AppColors.badgeColors(tone: DaysBadgeTone): BadgeColors =
    when (tone) {
        DaysBadgeTone.SHARED -> BadgeColors(primarySoft, onPrimarySoft, primary)
        DaysBadgeTone.PRIVATE -> BadgeColors(fill, textSecondary, textTertiary)
        DaysBadgeTone.CATEGORY -> BadgeColors(accentSoft, onAccentSoft, accent)
        DaysBadgeTone.SOON -> BadgeColors(dangerSoft, danger, danger)
        DaysBadgeTone.SUCCESS -> BadgeColors(successSoft, success, success)
    }

@ComponentPreviews
@Composable
private fun DaysBadgePreview() {
    DaysPreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            DaysBadge(text = "우리 둘이", tone = DaysBadgeTone.SHARED)
            DaysBadge(text = "나만 보기", tone = DaysBadgeTone.PRIVATE)
            DaysBadge(text = "매년", tone = DaysBadgeTone.CATEGORY)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            DaysBadge(text = "곧 도래", tone = DaysBadgeTone.SOON)
            DaysBadge(text = "완료", tone = DaysBadgeTone.SUCCESS)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            DaysBadge(text = "지민과 연결됨", tone = DaysBadgeTone.SUCCESS, size = DaysBadgeSize.MEDIUM, showDot = true)
            DaysBadge(text = "로맨틱 핑크", tone = DaysBadgeTone.SHARED, size = DaysBadgeSize.MEDIUM, showDot = true)
        }
    }
}
