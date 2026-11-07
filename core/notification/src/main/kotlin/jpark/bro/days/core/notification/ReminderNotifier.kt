package jpark.bro.days.core.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import jpark.bro.days.core.domain.reminder.DueReminder
import jpark.bro.days.core.model.AutoAnniversary
import jpark.bro.days.core.model.ItemType
import jpark.bro.days.core.navigation.AppIntents

/**
 * 기념일 · 일정 로컬 알림을 띄운다.
 * 알림마다 태그(항목 · 며칠 전 · 기념일 키)가 달라 같은 날 다시 띄워도 쌓이지 않고 바뀐다.
 * 알림 권한이 없으면 아무것도 하지 않는다.
 */
class ReminderNotifier(
    private val context: Context,
) {
    private val manager = NotificationManagerCompat.from(context)

    /** 채널: 기념일(디데이 · 자동 기념일) · 일정. 알림 설정 화면(7-3)이 채널별로 켜고 끈다 */
    fun createChannels() {
        manager.createNotificationChannelsCompat(
            listOf(
                channel(
                    ANNIVERSARY_CHANNEL,
                    R.string.notification_channel_anniversary_name,
                    R.string.notification_channel_anniversary_desc,
                ),
                channel(PLAN_CHANNEL, R.string.notification_channel_plan_name, R.string.notification_channel_plan_desc),
            ),
        )
    }

    /** 제목 = 항목 제목. 누르면 디데이는 상세, 일정은 수정 화면 (MainActivity) */
    fun notifyItem(reminder: DueReminder) {
        val item = reminder.item
        val text =
            when (reminder.offsetDays) {
                0 -> context.getString(R.string.notification_reminder_today)
                1 -> context.getString(R.string.notification_reminder_tomorrow)
                else -> context.getString(R.string.notification_reminder_days_left, reminder.offsetDays)
            }
        val channel = if (item.content.type == ItemType.PLAN) PLAN_CHANNEL else ANNIVERSARY_CHANNEL
        val tag = "item:${item.id}:${reminder.offsetDays}"
        val intent =
            openAppIntent()
                .putExtra(AppIntents.EXTRA_OPEN_ITEM_ID, item.id)
                .putExtra(AppIntents.EXTRA_OPEN_ITEM_TYPE, item.content.type.name)
        post(tag, channel, title = item.content.title, text = text, intent = intent)
    }

    /** [nicknames]: 생일 기념일의 사용자 id → 이름. [myUserId]의 생일은 "내 생일" */
    fun notifyAnniversary(
        anniversary: AutoAnniversary,
        nicknames: Map<String, String>,
        myUserId: String?,
    ) {
        val title =
            when (anniversary) {
                is AutoAnniversary.Days -> {
                    context.getString(R.string.notification_anniversary_days, anniversary.days)
                }

                is AutoAnniversary.Years -> {
                    context.getString(R.string.notification_anniversary_years, anniversary.years)
                }

                is AutoAnniversary.Birthday -> {
                    val nickname = nicknames[anniversary.userId]
                    if (anniversary.userId == myUserId || nickname == null) {
                        context.getString(R.string.notification_anniversary_my_birthday)
                    } else {
                        context.getString(R.string.notification_anniversary_birthday, nickname)
                    }
                }
            }
        post(
            tag = "anniversary:${anniversary.ruleKey}",
            channel = ANNIVERSARY_CHANNEL,
            title = title,
            text = context.getString(R.string.notification_anniversary_text),
            intent = openAppIntent(),
        )
    }

    private fun post(
        tag: String,
        channel: String,
        title: String,
        text: String,
        intent: Intent,
    ) {
        // 권한 확인은 notify와 같은 함수에 있어야 lint가 알아본다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (!manager.areNotificationsEnabled()) return
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                tag.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        val notification =
            NotificationCompat
                .Builder(context, channel)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build()
        manager.notify(tag, NOTIFICATION_ID, notification)
    }

    /** 앱을 연다. 이미 떠 있으면 그 화면에서 (MainActivity singleTop + onNewIntent) */
    private fun openAppIntent(): Intent =
        (context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

    private fun channel(
        id: String,
        name: Int,
        description: Int,
    ): NotificationChannelCompat =
        NotificationChannelCompat
            .Builder(id, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(context.getString(name))
            .setDescription(context.getString(description))
            .build()

    private companion object {
        const val ANNIVERSARY_CHANNEL = "anniversary"
        const val PLAN_CHANNEL = "plan"

        /** 알림은 태그로 구분한다 */
        const val NOTIFICATION_ID = 1
    }
}
