package jpark.bro.days.core.notification

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import jpark.bro.days.core.data.repository.ActivityNotifier
import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.model.ActivityContent
import kotlinx.coroutines.flow.first

/**
 * 상대 활동 시스템 알림 (동기화가 상대 변경을 받았을 때).
 * 알림 설정에서 껐거나 앱이 화면에 떠 있으면 띄우지 않는다 (알림 목록에는 이미 남아 있다)
 */
internal class PartnerActivityNotifier(
    private val context: Context,
    private val notifier: ReminderNotifier,
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
) : ActivityNotifier {
    override suspend fun notifyPartnerActivities(activities: List<ActivityContent>) {
        if (!settingsRepository.observeNotificationSettings().first().activityEnabled) return
        if (ProcessLifecycleOwner
                .get()
                .lifecycle.currentState
                .isAtLeast(Lifecycle.State.STARTED)
        ) {
            return
        }
        val partnerName =
            profileRepository.observePartnerProfile().first()?.nickname
                ?: context.getString(R.string.notification_activity_partner_unknown)
        activities.forEach { notifier.notifyPartnerActivity(it, partnerName) }
    }
}
