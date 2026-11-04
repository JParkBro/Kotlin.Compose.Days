package jpark.bro.days.core.firebase.config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import jpark.bro.days.core.common.suspendRunCatching
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * 원격 차단 스위치 (infra-and-billing-protection.md 5장). 이상 사용량이 보이면 콘솔에서 꺼서 바로 멈춘다.
 * 값은 마지막으로 활성화된 설정(기기에 저장됨) 또는 기본값이다. fetch는 무료이고, 앱이 화면에 올라올 때 [refresh]한다.
 * 나머지 스위치(upload_enabled · kakao_login_enabled · min_supported_version)는 해당 기능을 만들 때 추가한다.
 */
class RemoteFlags(
    private val remoteConfig: FirebaseRemoteConfig,
) {
    private val syncEnabledState = MutableStateFlow(remoteConfig.getBoolean(SYNC_ENABLED))

    /** false면 동기화 리스너 · Push · Pull을 모두 멈춘다 */
    val syncEnabled: StateFlow<Boolean> = syncEnabledState.asStateFlow()

    /** 실패해도 마지막 값을 그대로 쓴다 (네트워크 없음 등) */
    suspend fun refresh() {
        suspendRunCatching { remoteConfig.fetchAndActivate().await() }
        syncEnabledState.value = remoteConfig.getBoolean(SYNC_ENABLED)
    }

    companion object {
        const val SYNC_ENABLED = "sync_enabled"

        /** 콘솔에 값이 없거나 한 번도 받지 못했을 때 */
        val DEFAULTS: Map<String, Any> = mapOf(SYNC_ENABLED to true)
    }
}
