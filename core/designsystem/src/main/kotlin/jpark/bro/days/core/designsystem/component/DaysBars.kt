package jpark.bro.days.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.R
import jpark.bro.days.core.designsystem.preview.ComponentPreviews
import jpark.bro.days.core.designsystem.preview.DaysPreviewSurface

/**
 * 상단 헤더. 평소 bg, 내용이 스크롤되면([isScrolled]) surface + 아래 border.
 * 상태바 여백은 화면의 Scaffold가 처리한다.
 */
@Composable
fun DaysTopBar(
    title: String,
    modifier: Modifier = Modifier,
    isScrolled: Boolean = false,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = AppThemeTokens.colors
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(if (isScrolled) colors.surface else colors.bg),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(horizontal = AppSpacing.marginMobile),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            navigationIcon?.invoke()
            Text(
                text = title,
                style = AppThemeTokens.typography.headlineMd,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .weight(1f)
                        .semantics { heading() },
            )
            actions()
        }
        if (isScrolled) DaysDivider()
    }
}

/** 하단 탭 항목 */
@Immutable
data class DaysTab(
    val label: String,
    @DrawableRes val icon: Int,
)

/** 하단 탭바. surface + 상단 border, 활성 primary · 비활성 text-tertiary */
@Composable
fun DaysBottomBar(
    tabs: List<DaysTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    Column(modifier = modifier) {
        DaysDivider()
        NavigationBar(containerColor = colors.surface, tonalElevation = 0.dp) {
            tabs.forEachIndexed { index, tab ->
                NavigationBarItem(
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    icon = { Icon(painter = painterResource(tab.icon), contentDescription = null) },
                    label = { Text(text = tab.label, style = AppThemeTokens.typography.labelSm) },
                    colors =
                        NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.primary,
                            selectedTextColor = colors.primary,
                            unselectedIconColor = colors.textTertiary,
                            unselectedTextColor = colors.textTertiary,
                            indicatorColor = colors.primarySoft,
                        ),
                )
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun DaysBarsPreview() {
    DaysPreviewSurface {
        DaysTopBar(title = "설정")
        DaysTopBar(
            title = "디데이 상세",
            isScrolled = true,
            navigationIcon = { DaysIconButton(icon = R.drawable.ic_close, contentDescription = "닫기", onClick = {}) },
            actions = { DaysSoftButton(text = "수정", onClick = {}, size = DaysButtonSize.SMALL) },
        )
        DaysBottomBar(
            tabs =
                listOf(
                    DaysTab("홈", R.drawable.ic_favorite),
                    DaysTab("캘린더", R.drawable.ic_star),
                    DaysTab("설정", R.drawable.ic_person),
                ),
            selectedIndex = 0,
            onSelect = {},
        )
    }
}
