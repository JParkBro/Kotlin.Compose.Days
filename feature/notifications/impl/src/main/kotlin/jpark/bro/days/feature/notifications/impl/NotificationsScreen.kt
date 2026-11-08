package jpark.bro.days.feature.notifications.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import jpark.bro.days.core.designsystem.AppShapes
import jpark.bro.days.core.designsystem.AppSpacing
import jpark.bro.days.core.designsystem.AppTheme
import jpark.bro.days.core.designsystem.AppThemeTokens
import jpark.bro.days.core.designsystem.component.DaysButtonSize
import jpark.bro.days.core.designsystem.component.DaysIconBadge
import jpark.bro.days.core.designsystem.component.DaysIconBadgeTone
import jpark.bro.days.core.designsystem.component.DaysIconButton
import jpark.bro.days.core.designsystem.component.DaysSoftButton
import jpark.bro.days.core.designsystem.component.DaysToastHost
import jpark.bro.days.core.designsystem.component.DaysTopBar
import jpark.bro.days.core.designsystem.preview.ScreenPreviews
import jpark.bro.days.core.model.ActivityType
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.ui.ObserveEvents
import jpark.bro.days.feature.item.api.ItemDetailKey
import jpark.bro.days.feature.item.api.ItemEditKey
import jpark.bro.days.feature.settings.api.NotificationSettingsKey
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.days.core.designsystem.R as DesignR

@Composable
internal fun NotificationsRoot(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val markFailed = stringResource(R.string.notifications_list_mark_failed)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is NotificationsEvent.OpenItem -> {
                onNavigate(
                    when (event.itemType) {
                        ItemType.DDAY -> ItemDetailKey(event.itemId)
                        ItemType.PLAN -> ItemEditKey(itemId = event.itemId, type = ItemType.PLAN, presetDate = null)
                    },
                )
            }

            NotificationsEvent.OpenSettings -> {
                onNavigate(NotificationSettingsKey)
            }

            NotificationsEvent.MarkReadFailed -> {
                toastHostState.showSnackbar(markFailed)
            }

            NotificationsEvent.NavigateBack -> {
                onBack()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotificationsScreen(state = state, onAction = viewModel::onAction)
        DaysToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 알림 목록 (시안 notifications): 오늘 · 이번 주 · 이전, 읽지 않은 알림 강조 */
@Composable
private fun NotificationsScreen(
    state: NotificationsState,
    onAction: (NotificationsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg),
    ) {
        DaysTopBar(
            title = stringResource(R.string.notifications_list_title),
            isScrolled = listState.canScrollBackward,
            navigationIcon = {
                DaysIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.notifications_list_back),
                    onClick = { onAction(NotificationsAction.BackClicked) },
                )
            },
            actions = {
                if (state.unreadCount > 0) {
                    DaysIconButton(
                        icon = DesignR.drawable.ic_check,
                        contentDescription = stringResource(R.string.notifications_list_mark_all_read),
                        onClick = { onAction(NotificationsAction.MarkAllReadClicked) },
                        modifier = Modifier.testTag("notifications_mark_all_read"),
                    )
                }
            },
        )
        if (state.isLoading) return@Column
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            if (state.sections.isEmpty()) {
                item(key = "empty") { EmptyNotifications() }
            }
            state.sections.forEach { section ->
                item(key = "header_${section.group}") { SectionHeader(section.group) }
                items(section.rows, key = { it.id }) { row ->
                    ActivityCard(row = row, onClick = { onAction(NotificationsAction.RowClicked(row.id)) })
                }
            }
            item(key = "footer") {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = AppSpacing.lg, bottom = AppSpacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    DaysSoftButton(
                        text = stringResource(R.string.notifications_list_settings),
                        onClick = { onAction(NotificationsAction.SettingsClicked) },
                        size = DaysButtonSize.SMALL,
                        modifier = Modifier.testTag("notifications_settings"),
                    )
                    Text(
                        text = stringResource(R.string.notifications_list_retention),
                        style = AppThemeTokens.typography.bodySm,
                        color = AppThemeTokens.colors.textTertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(group: ActivityGroup) {
    val typography = AppThemeTokens.typography
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.md, start = AppSpacing.xs, end = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(group.label),
            style = typography.headlineSm,
            color = AppThemeTokens.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        if (group == ActivityGroup.THIS_WEEK) {
            Text(
                text = stringResource(R.string.notifications_list_group_this_week_desc),
                style = typography.labelMd,
                color = AppThemeTokens.colors.textSecondary,
            )
        }
    }
}

/** 읽지 않은 알림은 primary-soft 배경 + 점 */
@Composable
private fun ActivityCard(
    row: ActivityRow,
    onClick: () -> Unit,
) {
    val colors = AppThemeTokens.colors
    val unreadLabel = stringResource(R.string.notifications_list_unread)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(AppShapes.lg)
                .background(if (row.isRead) colors.surface else colors.primarySoft)
                .border(1.dp, if (row.isRead) colors.border else colors.primarySoft, AppShapes.lg)
                .clickable(onClick = onClick)
                .then(if (row.isRead) Modifier else Modifier.semantics { contentDescription = unreadLabel })
                .padding(AppSpacing.md)
                .testTag("notifications_row_${row.id}"),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            DaysIconBadge(
                icon = row.icon,
                tone = if (row.isRead) DaysIconBadgeTone.DEFAULT else DaysIconBadgeTone.SHARED,
            )
            if (!row.isRead) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(colors.primary),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = row.title(), style = AppThemeTokens.typography.labelLg, color = colors.textPrimary)
            Text(text = row.subtitle(), style = AppThemeTokens.typography.bodySm, color = colors.textSecondary)
        }
    }
}

@Composable
private fun EmptyNotifications() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = AppSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DaysIconBadge(icon = DesignR.drawable.ic_notifications, tone = DaysIconBadgeTone.SHARED)
        Text(
            text = stringResource(R.string.notifications_list_empty_title),
            style = AppThemeTokens.typography.headlineSm,
            color = AppThemeTokens.colors.textPrimary,
        )
        Text(
            text = stringResource(R.string.notifications_list_empty_desc),
            style = AppThemeTokens.typography.bodySm,
            color = AppThemeTokens.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

private val ActivityGroup.label: Int
    get() =
        when (this) {
            ActivityGroup.TODAY -> R.string.notifications_list_group_today
            ActivityGroup.THIS_WEEK -> R.string.notifications_list_group_this_week
            ActivityGroup.EARLIER -> R.string.notifications_list_group_earlier
        }

/** product-spec 9장 시나리오: 오늘 = 2026.11.05 */
@Suppress("MagicNumber") // 샘플 데이터
private val PreviewRows =
    listOf(
        ActivitySection(
            ActivityGroup.TODAY,
            listOf(
                ActivityRow(
                    id = "1",
                    type = ActivityType.ITEM_ADDED,
                    itemType = ItemType.PLAN,
                    itemTitle = "성수 저녁",
                    actorName = "지민",
                    offsetDays = null,
                    anniversary = null,
                    elapsed = Elapsed.JustNow,
                    isRead = false,
                    target = OpenTarget("dinner", ItemType.PLAN),
                ),
                ActivityRow(
                    id = "2",
                    type = ActivityType.ANNIVERSARY,
                    itemType = null,
                    itemTitle = null,
                    actorName = null,
                    offsetDays = null,
                    anniversary = AnniversaryLabel.Days(600),
                    elapsed = Elapsed.Hours(2),
                    isRead = true,
                    target = null,
                ),
            ),
        ),
        ActivitySection(
            ActivityGroup.THIS_WEEK,
            listOf(
                ActivityRow(
                    id = "3",
                    type = ActivityType.REMINDER,
                    itemType = ItemType.DDAY,
                    itemTitle = "토익 시험",
                    actorName = null,
                    offsetDays = 7,
                    anniversary = null,
                    elapsed = Elapsed.Yesterday,
                    isRead = false,
                    target = OpenTarget("toeic", ItemType.DDAY),
                ),
            ),
        ),
    )

@ScreenPreviews
@Composable
private fun NotificationsScreenPreview() {
    AppTheme {
        NotificationsScreen(
            state = NotificationsState(isLoading = false, sections = PreviewRows, unreadCount = 2),
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun NotificationsEmptyPreview() {
    AppTheme {
        NotificationsScreen(state = NotificationsState(isLoading = false), onAction = {})
    }
}
