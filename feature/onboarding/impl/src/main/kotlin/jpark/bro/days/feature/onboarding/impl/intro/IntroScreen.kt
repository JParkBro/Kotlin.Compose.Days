package jpark.bro.days.feature.onboarding.impl.intro

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysPrimaryButton
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.onboarding.impl.R
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.days.core.designsystem.R as DesignR

/** 소개 (product-spec 3-2: 2~3장 스와이프) */
@Composable
internal fun IntroRoot(
    onNavigateToLogin: () -> Unit,
    viewModel: IntroViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            IntroEvent.NavigateToLogin -> onNavigateToLogin()
        }
    }

    IntroScreen(state = state, onAction = viewModel::onAction)
}

private enum class IntroPage(
    @param:DrawableRes val icon: Int,
    @param:StringRes val title: Int,
    @param:StringRes val body: Int,
) {
    COUNT(DesignR.drawable.ic_calendar, R.string.onboarding_intro_title_count, R.string.onboarding_intro_body_count),
    ANNIVERSARY(
        DesignR.drawable.ic_cake,
        R.string.onboarding_intro_title_anniversary,
        R.string.onboarding_intro_body_anniversary,
    ),
    COUPLE(DesignR.drawable.ic_favorite, R.string.onboarding_intro_title_couple, R.string.onboarding_intro_body_couple),
}

@Composable
private fun IntroScreen(
    state: IntroState,
    onAction: (IntroAction) -> Unit,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
) {
    val colors = AppThemeTokens.colors
    val pages = IntroPage.entries
    val pagerState = rememberPagerState(initialPage = initialPage) { pages.size }
    val scope = rememberCoroutineScope()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.bg)
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        if (state.isLoading) return@Column
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = stringResource(R.string.onboarding_intro_skip),
                style = AppThemeTokens.typography.labelMd,
                color = colors.textSecondary,
                modifier =
                    Modifier
                        .clip(AppShapes.full)
                        .background(colors.fill)
                        .clickable { onAction(IntroAction.StartClicked) }
                        .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                        .testTag("intro_skip"),
            )
        }
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("intro_pager"),
        ) { index -> IntroPageContent(page = pages[index]) }
        PageIndicator(pagerState = pagerState)
        val isLast = pagerState.currentPage == pages.lastIndex
        DaysPrimaryButton(
            text = stringResource(if (isLast) R.string.onboarding_intro_start else R.string.onboarding_intro_next),
            onClick = {
                if (isLast) {
                    onAction(IntroAction.StartClicked)
                } else {
                    // 넘기기만 하는 화면 안 동작이라 ViewModel을 거치지 않는다
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.marginMobile)
                    .testTag("intro_next"),
        )
    }
}

@Composable
private fun IntroPageContent(
    page: IntroPage,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.marginMobile),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(180.dp)
                    .clip(AppShapes.xl)
                    .background(colors.primarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(page.icon),
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(80.dp),
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.xxl))
        Text(
            text = stringResource(page.title),
            style = AppThemeTokens.typography.headlineLg,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(page.body),
            style = AppThemeTokens.typography.bodyMd,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = AppSpacing.sm),
        )
    }
}

@Composable
private fun PageIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val description =
        stringResource(R.string.onboarding_intro_page, pagerState.currentPage + 1, pagerState.pageCount)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = AppSpacing.md)
                .semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.CenterHorizontally),
    ) {
        repeat(pagerState.pageCount) { index ->
            val selected = index == pagerState.currentPage
            Box(
                modifier =
                    Modifier
                        .height(8.dp)
                        .width(if (selected) 24.dp else 8.dp)
                        .clip(AppShapes.full)
                        .background(if (selected) colors.primary else colors.fill),
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun IntroScreenPreview() {
    AppTheme { IntroScreen(state = IntroState(isLoading = false), onAction = {}) }
}

@ScreenPreviews
@Composable
private fun IntroLastPagePreview() {
    AppTheme { IntroScreen(state = IntroState(isLoading = false), onAction = {}, initialPage = 2) }
}
