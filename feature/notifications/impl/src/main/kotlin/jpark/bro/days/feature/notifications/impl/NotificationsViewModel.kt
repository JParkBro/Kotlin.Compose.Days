package jpark.bro.days.feature.notifications.impl

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.ActivityRepository
import jpark.bro.days.core.data.repository.ItemRepository
import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.model.ActivityLog
import jpark.bro.days.core.model.ActivityType
import jpark.bro.days.core.model.AnniversaryRule
import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.UserProfile
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** 알림 목록: 상대 활동 + 보낸 기념일 · 일정 알림 (최근 30일, 기기에만) */
internal class NotificationsViewModel(
    private val activityRepository: ActivityRepository,
    itemRepository: ItemRepository,
    profileRepository: ProfileRepository,
    private val clock: Clock,
) : MviViewModel<NotificationsState, NotificationsAction, NotificationsEvent>(NotificationsState()) {
    init {
        combine(
            activityRepository.observeActivities(),
            itemRepository.observeItems(),
            profileRepository.observeMyProfile(),
            profileRepository.observePartnerProfile(),
        ) { activities, items, me, partner ->
            val visibleItemIds = items.mapTo(mutableSetOf()) { it.id }
            val profiles = listOfNotNull(me, partner)
            val rows = activities.map { it.toRow(visibleItemIds, profiles, me?.userId) }
            setState {
                copy(
                    isLoading = false,
                    sections =
                        rows.groupBy { it.group }.map { (group, list) ->
                            ActivitySection(group, list.map { it.row })
                        },
                    unreadCount = activities.count { !it.isRead },
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: NotificationsAction) {
        when (action) {
            is NotificationsAction.RowClicked -> open(action.id)
            NotificationsAction.MarkAllReadClicked -> markAllRead()
            NotificationsAction.SettingsClicked -> sendEvent(NotificationsEvent.OpenSettings)
            NotificationsAction.BackClicked -> sendEvent(NotificationsEvent.NavigateBack)
        }
    }

    private fun open(id: String) {
        val row = currentState.sections.flatMap { it.rows }.firstOrNull { it.id == id } ?: return
        if (!row.isRead) viewModelScope.launch { activityRepository.markRead(id) }
        row.target?.let { sendEvent(NotificationsEvent.OpenItem(it.itemId, it.itemType)) }
    }

    private fun markAllRead() {
        viewModelScope.launch {
            activityRepository.markAllRead().onFailure { sendEvent(NotificationsEvent.MarkReadFailed) }
        }
    }

    private fun ActivityLog.toRow(
        visibleItemIds: Set<String>,
        profiles: List<UserProfile>,
        myUserId: String?,
    ): GroupedRow {
        val nicknames = profiles.associate { it.userId to it.nickname }
        val canOpen = content.type != ActivityType.ITEM_DELETED && content.itemId in visibleItemIds
        val target =
            if (canOpen) content.itemType?.let { type -> content.itemId?.let { OpenTarget(it, type) } } else null
        val row =
            ActivityRow(
                id = id,
                type = content.type,
                itemType = content.itemType,
                itemTitle = content.itemTitle,
                actorName = content.actorUserId?.let { nicknames[it].orEmpty() },
                offsetDays = content.offsetDays,
                anniversary = content.ruleKey?.let { AutoAnniversary.ruleOf(it) }?.toLabel(nicknames, myUserId),
                elapsed = elapsedSince(createdAt),
                isRead = isRead,
                target = target,
            )
        return GroupedRow(groupOf(createdAt), row)
    }

    private fun elapsedSince(createdAt: Long): Elapsed {
        val now = Instant.now(clock)
        val then = Instant.ofEpochMilli(createdAt)
        val minutes = Duration.between(then, now).toMinutes()
        return when (val days = daysAgo(createdAt)) {
            0L -> {
                when {
                    minutes < 1 -> Elapsed.JustNow
                    minutes < MINUTES_PER_HOUR -> Elapsed.Minutes(minutes)
                    else -> Elapsed.Hours(minutes / MINUTES_PER_HOUR)
                }
            }

            1L -> {
                Elapsed.Yesterday
            }

            else -> {
                Elapsed.Days(days)
            }
        }
    }

    private fun groupOf(createdAt: Long): ActivityGroup =
        when (daysAgo(createdAt)) {
            0L -> ActivityGroup.TODAY
            in 1L until DAYS_PER_WEEK -> ActivityGroup.THIS_WEEK
            else -> ActivityGroup.EARLIER
        }

    /** 날짜(그 지역 기준)로 며칠 전인지. 미래 기록(기기 시각 변경)은 오늘로 */
    private fun daysAgo(createdAt: Long): Long {
        val day = Instant.ofEpochMilli(createdAt).atZone(clock.zone).toLocalDate()
        return maxOf(0L, ChronoUnit.DAYS.between(day, LocalDate.now(clock)))
    }

    private class GroupedRow(
        val group: ActivityGroup,
        val row: ActivityRow,
    )

    private companion object {
        const val MINUTES_PER_HOUR = 60L
        const val DAYS_PER_WEEK = 7L
    }
}

private fun AnniversaryRule.toLabel(
    nicknames: Map<String, String>,
    myUserId: String?,
): AnniversaryLabel =
    when (this) {
        is AnniversaryRule.Days -> {
            AnniversaryLabel.Days(days)
        }

        is AnniversaryRule.Years -> {
            AnniversaryLabel.Years(years)
        }

        is AnniversaryRule.Birthday -> {
            AnniversaryLabel.Birthday(
                if (userId ==
                    myUserId
                ) {
                    null
                } else {
                    nicknames[userId].orEmpty()
                },
            )
        }
    }
