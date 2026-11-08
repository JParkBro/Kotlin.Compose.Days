package jpark.bro.days.feature.settings.impl.notificationsettings

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.CoupleRepository
import jpark.bro.days.core.data.repository.OnboardingRepository
import jpark.bro.days.core.data.repository.ProfileRepository
import jpark.bro.days.core.data.repository.SettingsRepository
import jpark.bro.days.core.model.MarketingConsent
import jpark.bro.days.core.model.NotificationSettings
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

/**
 * 알림 설정 (product-spec 6장).
 * 기념일 · 일정 · 상대 활동 알림과 알림 시각은 기기에 저장하고, 마케팅 수신 동의는 서버(`users/{uid}`)에서 한 번 읽고 바꿀 때 쓴다.
 */
internal class NotificationSettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val onboardingRepository: OnboardingRepository,
    coupleRepository: CoupleRepository,
    profileRepository: ProfileRepository,
    private val clock: Clock,
) : MviViewModel<NotificationSettingsState, NotificationSettingsAction, NotificationSettingsEvent>(
        NotificationSettingsState(),
    ) {
    init {
        settingsRepository
            .observeNotificationSettings()
            .onEach { settings -> setState { copy(isLoading = false, settings = settings) } }
            .launchIn(viewModelScope)
        combine(coupleRepository.observeCouple(), profileRepository.observePartnerProfile()) { couple, partner ->
            couple?.let { partner?.nickname.orEmpty() }
        }.onEach { name -> setState { copy(partnerName = name) } }
            .launchIn(viewModelScope)
        loadMarketing()
    }

    override fun onAction(action: NotificationSettingsAction) {
        when (action) {
            NotificationSettingsAction.BackClicked -> {
                sendEvent(NotificationSettingsEvent.NavigateBack)
            }

            is NotificationSettingsAction.NotificationsChecked -> {
                setState {
                    copy(
                        notificationsEnabled = action.enabled,
                    )
                }
            }

            NotificationSettingsAction.DeviceSettingsClicked -> {
                sendEvent(NotificationSettingsEvent.OpenDeviceSettings)
            }

            is NotificationSettingsAction.AnniversaryToggled -> {
                save { copy(anniversaryEnabled = action.enabled) }
            }

            is NotificationSettingsAction.PlanToggled -> {
                save { copy(planEnabled = action.enabled) }
            }

            is NotificationSettingsAction.ActivityToggled -> {
                save { copy(activityEnabled = action.enabled) }
            }

            NotificationSettingsAction.SendHourClicked -> {
                setState { copy(isHourSheetVisible = true) }
            }

            is NotificationSettingsAction.SendHourSelected -> {
                selectHour(action.hour)
            }

            NotificationSettingsAction.HourSheetDismissed -> {
                setState { copy(isHourSheetVisible = false) }
            }

            is NotificationSettingsAction.MarketingToggled -> {
                setMarketing(action.agreed)
            }

            NotificationSettingsAction.MarketingRetryClicked -> {
                loadMarketing()
            }
        }
    }

    private fun selectHour(hour: Int) {
        setState { copy(isHourSheetVisible = false) }
        save { copy(sendHour = hour) }
    }

    /** 저장하면 기기 설정 스트림이 화면을 바꾼다 */
    private fun save(change: NotificationSettings.() -> NotificationSettings) {
        val updated = currentState.settings.change()
        viewModelScope.launch {
            settingsRepository
                .setNotificationSettings(updated)
                .onFailure { sendEvent(NotificationSettingsEvent.SaveFailed) }
        }
    }

    private fun loadMarketing() {
        setState { copy(marketing = MarketingRow.Loading) }
        viewModelScope.launch {
            onboardingRepository
                .marketingConsent()
                .onSuccess { consent -> setState { copy(marketing = consent.toRow()) } }
                .onFailure { setState { copy(marketing = MarketingRow.Failed) } }
        }
    }

    private fun setMarketing(agreed: Boolean) {
        val row = currentState.marketing as? MarketingRow.Loaded ?: return
        if (row.isSaving || row.agreed == agreed) return
        setState { copy(marketing = row.copy(isSaving = true)) }
        viewModelScope.launch {
            onboardingRepository
                .setMarketingConsent(agreed)
                .onSuccess { consent ->
                    setState { copy(marketing = consent.toRow()) }
                    sendEvent(NotificationSettingsEvent.MarketingChanged(agreed, consent.updatedAt.toDate() ?: today()))
                }.onFailure {
                    setState { copy(marketing = row) }
                    sendEvent(NotificationSettingsEvent.MarketingSaveFailed)
                }
        }
    }

    private fun MarketingConsent.toRow(): MarketingRow.Loaded =
        MarketingRow.Loaded(agreed = agreed, agreedOn = if (agreed) updatedAt.toDate() else null)

    private fun Long?.toDate(): LocalDate? = this?.let { Instant.ofEpochMilli(it).atZone(clock.zone).toLocalDate() }

    private fun today(): LocalDate = LocalDate.now(clock)
}
