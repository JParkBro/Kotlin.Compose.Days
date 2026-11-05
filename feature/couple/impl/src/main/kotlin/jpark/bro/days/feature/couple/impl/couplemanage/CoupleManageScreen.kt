package jpark.bro.days.feature.couple.impl.couplemanage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysBadge
import jpark.bro.days.core.designsystem.component.DaysBadgeSize
import jpark.bro.days.core.designsystem.component.DaysBadgeTone
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysCard
import jpark.bro.days.core.designsystem.component.DaysIconBadge
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysListItem
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.core.designsystem.component.DaysSwitch
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.model.Couple
import jpark.bro.days.core.model.UserProfile
import jpark.bro.days.core.ui.DaysDatePickerDialog
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.core.ui.ProfileAvatar
import jpark.bro.days.core.ui.toDisplayText
import jpark.bro.days.feature.couple.impl.R
import jpark.bro.days.feature.couple.impl.couplemanage.components.UnpairSheet
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun CoupleManageRoot(
    onOpenAnniversaries: () -> Unit,
    onBack: () -> Unit,
    viewModel: CoupleManageViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val updateFailed = stringResource(R.string.couple_manage_update_failed)
    val unpairFailed = stringResource(R.string.couple_unpair_failed)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            CoupleManageEvent.UpdateFailed -> toastHostState.showSnackbar(updateFailed)
            CoupleManageEvent.UnpairFailed -> toastHostState.showSnackbar(unpairFailed)
            CoupleManageEvent.Disconnected -> onBack()
            CoupleManageEvent.OpenAnniversaries -> onOpenAnniversaries()
            CoupleManageEvent.NavigateBack -> onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CoupleManageScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 커플 관리 (product-spec 6장, 시안 couple-manage). 자동 기념일 숨김은 별도 목록, 나이 · 기념일 알림은 해당 단계에서 */
@Composable
private fun CoupleManageScreen(
    state: CoupleManageState,
    onAction: (CoupleManageAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val scrollState = rememberScrollState()
    val partnerName = state.partner?.nickname ?: stringResource(R.string.couple_manage_partner_unknown)
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.bg),
    ) {
        DaysTopBar(
            title = stringResource(R.string.couple_manage_title),
            isScrolled = scrollState.canScrollBackward,
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.couple_back),
                    onClick = { onAction(CoupleManageAction.BackClicked) },
                )
            },
        )
        val couple = state.couple ?: return@Column
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            PartnerCard(state = state, couple = couple, partnerName = partnerName)
            DaysCard {
                Row(
                    modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.couple_start_date),
                            style = AppThemeTokens.typography.labelMd,
                            color = colors.textSecondary,
                        )
                        Text(
                            text = couple.startDate.toDisplayText(),
                            style = AppThemeTokens.typography.headlineSm,
                            color = colors.textPrimary,
                            modifier = Modifier.testTag("manage_start_date"),
                        )
                    }
                    DaysSoftButton(
                        text = stringResource(R.string.couple_change),
                        onClick = { onAction(CoupleManageAction.StartDateClicked) },
                        enabled = !state.isWorking,
                        size = DaysButtonSize.SMALL,
                        modifier = Modifier.testTag("manage_change_date"),
                    )
                }
            }
            DaysCard {
                Row(
                    modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.couple_count_first_day),
                            style = AppThemeTokens.typography.labelLg,
                            color = colors.textPrimary,
                        )
                        Text(
                            text = stringResource(R.string.couple_count_first_day_description),
                            style = AppThemeTokens.typography.bodySm,
                            color = colors.textSecondary,
                        )
                    }
                    DaysSwitch(
                        checked = couple.countFirstDayAsOne,
                        onCheckedChange = { onAction(CoupleManageAction.CountFirstDayChanged(it)) },
                        enabled = !state.isWorking,
                        modifier = Modifier.testTag("manage_count_first_day"),
                    )
                }
            }
            DaysCard {
                DaysListItem(
                    title = stringResource(R.string.couple_manage_anniversaries),
                    subtitle = stringResource(R.string.couple_manage_anniversaries_desc),
                    onClick = { onAction(CoupleManageAction.AnniversariesClicked) },
                    leading = { DaysIconBadge(icon = DesignR.drawable.ic_redeem) },
                    modifier = Modifier.testTag("manage_anniversaries"),
                )
            }
            UnpairEntry(onClick = { onAction(CoupleManageAction.UnpairClicked) })
        }
    }

    if (state.isDatePickerOpen) {
        DaysDatePickerDialog(
            initial = state.couple?.startDate,
            maxDate = state.today,
            onConfirm = { onAction(CoupleManageAction.StartDateSelected(it)) },
            onDismiss = { onAction(CoupleManageAction.DatePickerDismissed) },
        )
    }
    if (state.isUnpairSheetOpen) {
        UnpairSheet(partnerName = partnerName, state = state, onAction = onAction)
    }
}

@Composable
private fun PartnerCard(
    state: CoupleManageState,
    couple: Couple,
    partnerName: String,
) {
    val colors = AppThemeTokens.colors
    DaysCard {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                ProfileAvatar(file = state.partnerPhoto, name = state.partner?.nickname)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = partnerName,
                        style = AppThemeTokens.typography.headlineSm,
                        color = colors.textPrimary,
                        modifier = Modifier.testTag("manage_partner_name"),
                    )
                    state.togetherDays?.let { days ->
                        Text(
                            text = stringResource(R.string.couple_manage_together, partnerName, days),
                            style = AppThemeTokens.typography.bodySm,
                            color = colors.textSecondary,
                        )
                    }
                }
                DaysBadge(
                    text = stringResource(R.string.couple_manage_connected),
                    tone = DaysBadgeTone.SUCCESS,
                    size = DaysBadgeSize.MEDIUM,
                    showDot = true,
                )
            }
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.default)
                        .background(colors.primarySoft)
                        .padding(AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                state.partner?.birthDate?.let {
                    InfoRow(DesignR.drawable.ic_cake, stringResource(R.string.couple_manage_birth_date), it)
                }
                InfoRow(
                    DesignR.drawable.ic_favorite,
                    stringResource(R.string.couple_manage_connected_since),
                    couple.startDate,
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: Int,
    label: String,
    date: LocalDate,
) {
    val colors = AppThemeTokens.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = colors.onPrimarySoft,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = label,
            style = AppThemeTokens.typography.bodyMd,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(text = date.toDisplayText(), style = AppThemeTokens.typography.labelLg, color = colors.textPrimary)
    }
}

/** 되돌리기 어려운 동작은 화면 맨 아래 (product-spec 6장) */
@Composable
private fun UnpairEntry(onClick: () -> Unit) {
    val colors = AppThemeTokens.colors
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.xl)
                .clip(AppShapes.default)
                .clickable(onClick = onClick)
                .padding(AppSpacing.md)
                .testTag("manage_unpair"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Icon(painterResource(DesignR.drawable.ic_heart_broken), contentDescription = null, tint = colors.danger)
            Text(
                text = stringResource(R.string.couple_manage_unpair),
                style = AppThemeTokens.typography.labelLg,
                color = colors.danger,
            )
        }
        Text(
            text = stringResource(R.string.couple_manage_unpair_description),
            style = AppThemeTokens.typography.bodySm,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

/** product-spec 9장: 현우 · 지민, 사귄 날 2025.06.14, 오늘 2026.11.05 */
private val PreviewState =
    CoupleManageState(
        isLoading = false,
        couple = Couple("couple-1", "user-partner", LocalDate.parse("2025-06-14"), countFirstDayAsOne = true),
        partner = UserProfile("user-partner", "지민", LocalDate.parse("1999-09-28")),
        today = LocalDate.parse("2026-11-05"),
    )

@ScreenPreviews
@Composable
private fun CoupleManagePreview() {
    AppTheme { CoupleManageScreen(state = PreviewState, onAction = {}) }
}
