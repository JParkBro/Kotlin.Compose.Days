package jpark.bro.days.feature.onboarding.impl.terms

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysCheckbox
import jpark.bro.days.core.designsystem.component.DaysDivider
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysPrimaryButton
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.onboarding.impl.R
import jpark.bro.days.feature.settings.api.PolicyType
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.days.core.designsystem.R as DesignR

/** 약관 동의 (product-spec 5장): 로그인 직후, 시작 방식 선택 전 */
@Composable
internal fun TermsRoot(
    onNavigateToStartMode: () -> Unit,
    onOpenPolicy: (PolicyType) -> Unit,
    onBack: () -> Unit,
    viewModel: TermsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val agreeFailed = stringResource(R.string.onboarding_terms_failed)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            TermsEvent.NavigateToStartMode -> onNavigateToStartMode()
            TermsEvent.NavigateBack -> onBack()
            is TermsEvent.OpenPolicy -> onOpenPolicy(event.type)
            TermsEvent.AgreeFailed -> toastHostState.showSnackbar(agreeFailed)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TermsScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun TermsScreen(
    state: TermsState,
    onAction: (TermsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.bg)
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        DaysTopBar(
            title = "",
            isScrolled = scrollState.canScrollBackward,
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.onboarding_terms_back),
                    onClick = { onAction(TermsAction.BackClicked) },
                )
            },
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = AppSpacing.marginMobile),
        ) {
            Text(
                text = stringResource(R.string.onboarding_terms_title),
                style = AppThemeTokens.typography.headlineLg,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.onboarding_terms_subtitle),
                style = AppThemeTokens.typography.bodyMd,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = AppSpacing.sm, bottom = AppSpacing.xl),
            )
            DaysCard {
                AllAgreeRow(checked = state.allChecked, onToggle = { onAction(TermsAction.AllToggled) })
                DaysDivider(inset = AppSpacing.marginMobile)
                Term.entries.forEach { term ->
                    TermRow(
                        term = term,
                        checked = term in state.checked,
                        onToggle = { onAction(TermsAction.Toggled(term)) },
                        onView = term.policy?.let { type -> { onAction(TermsAction.ViewPolicyClicked(type)) } },
                    )
                }
            }
        }
        DaysPrimaryButton(
            text = stringResource(R.string.onboarding_terms_agree),
            onClick = { onAction(TermsAction.AgreeClicked) },
            enabled = state.canAgree,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = AppSpacing.marginMobile, end = AppSpacing.marginMobile, top = AppSpacing.md)
                    .testTag("terms_agree"),
        )
        Text(
            text = stringResource(R.string.onboarding_terms_hint),
            style = AppThemeTokens.typography.bodySm,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.sm),
        )
    }
}

@Composable
private fun AllAgreeRow(
    checked: Boolean,
    onToggle: () -> Unit,
) {
    val colors = AppThemeTokens.colors
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() })
                .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md)
                .testTag("terms_all"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        DaysCheckbox(checked = checked, onCheckedChange = null)
        Column {
            Text(
                text = stringResource(R.string.onboarding_terms_all),
                style = AppThemeTokens.typography.headlineSm,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.onboarding_terms_all_desc),
                style = AppThemeTokens.typography.bodySm,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun TermRow(
    term: Term,
    checked: Boolean,
    onToggle: () -> Unit,
    onView: (() -> Unit)?,
) {
    val colors = AppThemeTokens.colors
    val prefix =
        stringResource(if (term.required) R.string.onboarding_terms_required else R.string.onboarding_terms_optional)
    val title = stringResource(term.title)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(end = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier
                    .weight(1f)
                    .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() })
                    .padding(start = AppSpacing.marginMobile, top = AppSpacing.sm, bottom = AppSpacing.sm)
                    .testTag("terms_${term.name.lowercase()}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            DaysCheckbox(checked = checked, onCheckedChange = null)
            Column {
                Text(
                    text =
                        buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = if (term.required) colors.primary else colors.textSecondary,
                                    fontWeight = FontWeight.Bold,
                                ),
                            ) { append(prefix) }
                            append(" ")
                            append(title)
                        },
                    style = AppThemeTokens.typography.labelLg,
                    color = colors.textPrimary,
                )
                if (term == Term.MARKETING) {
                    Text(
                        text = stringResource(R.string.onboarding_terms_marketing_desc),
                        style = AppThemeTokens.typography.bodySm,
                        color = colors.textSecondary,
                    )
                }
            }
        }
        if (onView != null) {
            Text(
                text = stringResource(R.string.onboarding_terms_view),
                style = AppThemeTokens.typography.labelMd,
                color = colors.textSecondary,
                modifier =
                    Modifier
                        .clickable(onClick = onView)
                        .padding(AppSpacing.md),
            )
        }
    }
}

@get:StringRes
private val Term.title: Int
    get() =
        when (this) {
            Term.AGE_OVER_14 -> R.string.onboarding_terms_age
            Term.SERVICE -> R.string.onboarding_terms_service
            Term.PRIVACY -> R.string.onboarding_terms_privacy
            Term.MARKETING -> R.string.onboarding_terms_marketing
        }

/** "보기"로 여는 정책 문서. 나이 확인 · 마케팅 수신은 문서가 따로 없다 */
private val Term.policy: PolicyType?
    get() =
        when (this) {
            Term.SERVICE -> PolicyType.TERMS
            Term.PRIVACY -> PolicyType.PRIVACY
            Term.AGE_OVER_14, Term.MARKETING -> null
        }

@ScreenPreviews
@Composable
private fun TermsScreenPreview() {
    AppTheme { TermsScreen(state = TermsState(), onAction = {}) }
}

@ScreenPreviews
@Composable
private fun TermsRequiredCheckedPreview() {
    AppTheme {
        TermsScreen(state = TermsState(checked = setOf(Term.AGE_OVER_14, Term.SERVICE, Term.PRIVACY)), onAction = {})
    }
}
