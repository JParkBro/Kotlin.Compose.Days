package jpark.bro.days.feature.settings.impl.supportdeveloper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.billing.SupportOffer
import jpark.bro.days.core.billing.SupportProduct
import jpark.bro.days.core.billing.rememberSupportPurchaseLauncher
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysDivider
import jpark.bro.days.core.designsystem.component.DaysIconBadge
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.component.DaysSecondaryButton
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.settings.impl.R
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun SupportDeveloperRoot(
    onBack: () -> Unit,
    viewModel: SupportDeveloperViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val thanks = stringResource(R.string.settings_support_thanks)
    val pending = stringResource(R.string.settings_support_pending)
    val failed = stringResource(R.string.settings_support_failed)
    val launchPurchase =
        rememberSupportPurchaseLauncher { viewModel.onAction(SupportDeveloperAction.PurchaseFinished(it)) }

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is SupportDeveloperEvent.LaunchPurchase -> launchPurchase(event.product)
            SupportDeveloperEvent.Thanks -> toastHostState.showSnackbar(thanks)
            SupportDeveloperEvent.PurchasePending -> toastHostState.showSnackbar(pending)
            SupportDeveloperEvent.PurchaseFailed -> toastHostState.showSnackbar(failed)
            SupportDeveloperEvent.NavigateBack -> onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SupportDeveloperScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 개발자 응원하기 (ads-strategy.md 6장). 시안 없음 */
@Composable
private fun SupportDeveloperScreen(
    state: SupportDeveloperState,
    onAction: (SupportDeveloperAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.bg),
    ) {
        DaysTopBar(
            title = stringResource(R.string.settings_support_title),
            isScrolled = scrollState.canScrollBackward,
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.settings_support_back),
                    onClick = { onAction(SupportDeveloperAction.BackClicked) },
                )
            },
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            Text(
                text = stringResource(R.string.settings_support_desc),
                style = typography.bodyMd,
                color = colors.textSecondary,
            )
            when {
                state.isLoading -> {
                    Text(
                        text = stringResource(R.string.settings_support_loading),
                        style = typography.bodySm,
                        color = colors.textTertiary,
                    )
                }

                state.offers.isEmpty() -> {
                    UnavailableCard(isLoadFailed = state.isLoadFailed, onAction = onAction)
                }

                else -> {
                    OfferList(offers = state.offers, enabled = !state.isPurchasing, onAction = onAction)
                }
            }
            Text(
                text = stringResource(R.string.settings_support_note),
                style = typography.bodySm,
                color = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun OfferList(
    offers: List<SupportOffer>,
    enabled: Boolean,
    onAction: (SupportDeveloperAction) -> Unit,
) {
    DaysCard {
        offers.forEachIndexed { index, offer ->
            if (index > 0) DaysDivider(inset = AppSpacing.marginMobile)
            DaysListItem(
                title = stringResource(offer.product.label),
                leading = { DaysIconBadge(icon = offer.product.icon) },
                trailing = {
                    DaysSoftButton(
                        text = offer.price,
                        onClick = { onAction(SupportDeveloperAction.ProductClicked(offer.product)) },
                        enabled = enabled,
                        size = DaysButtonSize.SMALL,
                    )
                },
                modifier = Modifier.testTag("support_${offer.product.name.lowercase()}"),
            )
        }
    }
}

/** 결제를 쓸 수 없거나(Play 연결 실패) 상품이 아직 등록되지 않음 */
@Composable
private fun UnavailableCard(
    isLoadFailed: Boolean,
    onAction: (SupportDeveloperAction) -> Unit,
) {
    val message = if (isLoadFailed) R.string.settings_support_load_failed else R.string.settings_support_unavailable
    DaysCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = stringResource(message),
                style = AppThemeTokens.typography.bodyMd,
                color = AppThemeTokens.colors.textPrimary,
            )
            if (isLoadFailed) {
                DaysSecondaryButton(
                    text = stringResource(R.string.settings_support_retry),
                    onClick = { onAction(SupportDeveloperAction.RetryClicked) },
                    size = DaysButtonSize.SMALL,
                    modifier = Modifier.testTag("support_retry"),
                )
            }
        }
    }
}

private val SupportProduct.label: Int
    get() =
        when (this) {
            SupportProduct.SMALL -> R.string.settings_support_small
            SupportProduct.MEDIUM -> R.string.settings_support_medium
            SupportProduct.LARGE -> R.string.settings_support_large
        }

private val SupportProduct.icon: Int
    get() =
        when (this) {
            SupportProduct.SMALL -> DesignR.drawable.ic_favorite
            SupportProduct.MEDIUM -> DesignR.drawable.ic_cake
            SupportProduct.LARGE -> DesignR.drawable.ic_redeem
        }

@ScreenPreviews
@Composable
private fun SupportDeveloperScreenPreview() {
    AppTheme {
        SupportDeveloperScreen(
            state =
                SupportDeveloperState(
                    isLoading = false,
                    offers =
                        listOf(
                            SupportOffer(SupportProduct.SMALL, "₩1,200"),
                            SupportOffer(SupportProduct.MEDIUM, "₩3,300"),
                            SupportOffer(SupportProduct.LARGE, "₩6,600"),
                        ),
                ),
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun SupportDeveloperUnavailablePreview() {
    AppTheme {
        SupportDeveloperScreen(state = SupportDeveloperState(isLoading = false), onAction = {})
    }
}
