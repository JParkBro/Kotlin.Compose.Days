package jpark.bro.days.feature.settings.impl.policydoc

import jpark.bro.days.core.ui.MviViewModel
import jpark.bro.days.feature.settings.api.PolicyDocKey

/** 정책 문서 (Firebase Hosting `policy-site/` 페이지를 웹뷰로). 설정 · 온보딩 약관 동의에서 연다 */
internal class PolicyDocViewModel(
    key: PolicyDocKey,
) : MviViewModel<PolicyDocState, PolicyDocAction, PolicyDocEvent>(PolicyDocState(type = key.type)) {
    override fun onAction(action: PolicyDocAction) {
        when (action) {
            PolicyDocAction.PageLoaded -> {
                setState { copy(isLoading = false) }
            }

            PolicyDocAction.PageFailed -> {
                setState { copy(isLoading = false, isFailed = true) }
            }

            PolicyDocAction.RetryClicked -> {
                setState {
                    copy(
                        isLoading = true,
                        isFailed = false,
                        reloadCount =
                            reloadCount + 1,
                    )
                }
            }

            PolicyDocAction.BackClicked -> {
                sendEvent(PolicyDocEvent.NavigateBack)
            }
        }
    }
}
