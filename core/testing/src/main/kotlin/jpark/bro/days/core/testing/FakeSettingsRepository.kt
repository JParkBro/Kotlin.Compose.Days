package jpark.bro.days.core.testing

import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.model.NotificationSettings
import jpark.bro.days.core.model.ThemeSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class FakeSettingsRepository(
    initial: ThemeSetting = ThemeSetting(),
) : SettingsRepository {
    private val theme = MutableStateFlow(initial)
    private val preview = MutableStateFlow<ThemeSetting?>(null)
    private val coupleBannerDismissed = MutableStateFlow(false)
    private val notificationSettings = MutableStateFlow(NotificationSettings())

    /** 현재 미리보기 값 (null이면 미리보기 아님) */
    val themePreview: ThemeSetting? get() = preview.value

    /** 인앱 업데이트 안내에서 "나중에"를 누른 날 */
    var updatePromptDismissedDate: LocalDate? = null

    /** true면 다음 저장이 실패한다 */
    var failNextSave: Boolean = false

    override fun observeThemeSetting(): Flow<ThemeSetting> = theme

    override fun observeEffectiveThemeSetting(): Flow<ThemeSetting> = combine(theme, preview) { saved, p -> p ?: saved }

    override suspend fun setThemeSetting(setting: ThemeSetting): Result<Unit> {
        if (failNextSave) {
            failNextSave = false
            return Result.failure(IllegalStateException("저장 실패 (테스트)"))
        }
        theme.value = setting
        return Result.success(Unit)
    }

    override fun setThemePreview(setting: ThemeSetting?) {
        preview.value = setting
    }

    override fun observeNotificationSettings(): Flow<NotificationSettings> = notificationSettings

    override suspend fun setNotificationSettings(settings: NotificationSettings): Result<Unit> {
        if (failNextSave) {
            failNextSave = false
            return Result.failure(IllegalStateException("저장 실패 (테스트)"))
        }
        require(settings.sendHour in NotificationSettings.SendHours)
        notificationSettings.value = settings
        return Result.success(Unit)
    }

    override fun observeCoupleBannerDismissed(): Flow<Boolean> = coupleBannerDismissed

    override suspend fun dismissCoupleBanner(): Result<Unit> {
        coupleBannerDismissed.value = true
        return Result.success(Unit)
    }

    override suspend fun updatePromptDismissedDate(): Result<LocalDate?> = Result.success(updatePromptDismissedDate)

    override suspend fun setUpdatePromptDismissedDate(date: LocalDate): Result<Unit> {
        updatePromptDismissedDate = date
        return Result.success(Unit)
    }
}
