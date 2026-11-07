package jpark.bro.days.feature.notifications.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import jpark.bro.days.core.model.ActivityType
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.designsystem.R as DesignR

/** 알림 목록 한 줄의 아이콘 · 문구 */
internal val ActivityRow.icon: Int
    get() =
        when (type) {
            ActivityType.ANNIVERSARY -> {
                if (anniversary is AnniversaryLabel.Birthday) DesignR.drawable.ic_cake else DesignR.drawable.ic_favorite
            }

            ActivityType.REMINDER -> {
                DesignR.drawable.ic_schedule
            }

            ActivityType.ITEM_ADDED, ActivityType.ITEM_UPDATED, ActivityType.ITEM_DELETED -> {
                if (itemType == ItemType.PLAN) DesignR.drawable.ic_event else DesignR.drawable.ic_calendar
            }
        }

@Composable
internal fun ActivityRow.title(): String {
    val title = itemTitle.orEmpty()
    return when (type) {
        ActivityType.ITEM_ADDED -> {
            partnerAction(R.string.notifications_list_activity_added)
        }

        ActivityType.ITEM_UPDATED -> {
            partnerAction(R.string.notifications_list_activity_updated)
        }

        ActivityType.ITEM_DELETED -> {
            partnerAction(R.string.notifications_list_activity_deleted)
        }

        ActivityType.REMINDER -> {
            when (val days = offsetDays ?: 0) {
                0 -> stringResource(R.string.notifications_list_reminder_today, title)
                1 -> stringResource(R.string.notifications_list_reminder_tomorrow, title)
                else -> stringResource(R.string.notifications_list_reminder_days_left, title, days)
            }
        }

        ActivityType.ANNIVERSARY -> {
            anniversary?.let { anniversaryText(it) }.orEmpty()
        }
    }
}

@Composable
private fun ActivityRow.partnerAction(text: Int): String {
    val name = actorName?.ifEmpty { null } ?: stringResource(R.string.notifications_list_partner_unknown)
    val kind =
        stringResource(
            if (itemType ==
                ItemType.PLAN
            ) {
                R.string.notifications_list_kind_plan
            } else {
                R.string.notifications_list_kind_dday
            },
        )
    return stringResource(text, name, kind)
}

@Composable
private fun anniversaryText(label: AnniversaryLabel): String =
    when (label) {
        is AnniversaryLabel.Days -> {
            stringResource(R.string.notifications_list_anniversary_days, label.days)
        }

        is AnniversaryLabel.Years -> {
            stringResource(R.string.notifications_list_anniversary_years, label.years)
        }

        is AnniversaryLabel.Birthday -> {
            label.name?.let {
                stringResource(
                    R.string.notifications_list_anniversary_birthday,
                    it.ifEmpty { stringResource(R.string.notifications_list_partner_unknown) },
                )
            } ?: stringResource(R.string.notifications_list_anniversary_my_birthday)
        }
    }

/** 상대 활동은 "항목 제목 · 3시간 전", 나머지는 지난 시간만 (기념일은 "자동 기념일 · …") */
@Composable
internal fun ActivityRow.subtitle(): String {
    val elapsedText = elapsed.text()
    val prefix =
        when (type) {
            ActivityType.ITEM_ADDED, ActivityType.ITEM_UPDATED, ActivityType.ITEM_DELETED -> itemTitle
            ActivityType.ANNIVERSARY -> stringResource(R.string.notifications_list_anniversary_desc)
            ActivityType.REMINDER -> null
        }
    return if (prefix.isNullOrEmpty()) elapsedText else "$prefix · $elapsedText"
}

@Composable
private fun Elapsed.text(): String =
    when (this) {
        Elapsed.JustNow -> stringResource(R.string.notifications_list_elapsed_just_now)
        is Elapsed.Minutes -> stringResource(R.string.notifications_list_elapsed_minutes, minutes.toInt())
        is Elapsed.Hours -> stringResource(R.string.notifications_list_elapsed_hours, hours.toInt())
        Elapsed.Yesterday -> stringResource(R.string.notifications_list_elapsed_yesterday)
        is Elapsed.Days -> stringResource(R.string.notifications_list_elapsed_days, days.toInt())
    }
