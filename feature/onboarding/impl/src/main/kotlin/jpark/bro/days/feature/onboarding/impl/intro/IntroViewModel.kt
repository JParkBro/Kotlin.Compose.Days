package jpark.bro.days.feature.onboarding.impl.intro

import androidx.lifecycle.viewModelScope
import jpark.bro.days.core.data.repository.OnboardingRepository
import jpark.bro.days.core.ui.MviViewModel
import kotlinx.coroutines.launch

/** 소개는 기기마다 한 번. 본 뒤로는(로그아웃 · 탈퇴 후 포함) 로그인부터 */
internal class IntroViewModel(
    private val onboardingRepository: OnboardingRepository,
) : MviViewModel<IntroState, IntroAction, IntroEvent>(IntroState()) {
    init {
        viewModelScope.launch {
            if (onboardingRepository.isIntroSeen()) {
                sendEvent(IntroEvent.NavigateToLogin)
            } else {
                setState { copy(isLoading = false) }
            }
        }
    }

    override fun onAction(action: IntroAction) {
        when (action) {
            IntroAction.StartClicked -> {
                viewModelScope.launch {
                    onboardingRepository.markIntroSeen()
                    sendEvent(IntroEvent.NavigateToLogin)
                }
            }
        }
    }
}
