package jpark.bro.days.core.firebase

import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.functions.functions
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.firebase.storage.storage
import jpark.bro.days.core.firebase.config.RemoteFlags

/** Firebase Auth · Firestore · Functions · Storage 에뮬레이터 포트 (firebase/firebase.json과 같게) */
private const val AUTH_EMULATOR_PORT = 9099
private const val FIRESTORE_EMULATOR_PORT = 8080
private const val FUNCTIONS_EMULATOR_PORT = 5001
private const val STORAGE_EMULATOR_PORT = 9199

/** 함수 리전 (firebase/functions/src/index.ts의 setGlobalOptions와 같게) */
internal const val FUNCTIONS_REGION = "asia-northeast3"

/** Remote Config를 다시 받는 최소 간격. 차단 스위치는 다음 앱 실행 · 포그라운드 진입 때 반영된다 */
private const val REMOTE_CONFIG_FETCH_INTERVAL_SECONDS = 3_600L

/**
 * 앱 시작 시 한 번 (Application.onCreate, Koin보다 먼저). Firebase 인스턴스를 쓰기 전에 설정해야 한다.
 * - App Check: prod는 Play Integrity, dev는 디버그 제공자 (첫 실행 때 logcat의 디버그 토큰을 콘솔에 등록)
 * - Firestore 캐시는 메모리만: Room이 원본이라 디스크에 중복 저장하지 않는다 (firebase-cost-strategy.md 4장)
 * - Remote Config: 차단 스위치 기본값 · 최소 fetch 간격 (에뮬레이터가 없어 dev도 실제 프로젝트에서 받는다, 무료)
 * - [emulatorHost]가 있으면 Auth · Firestore · Functions · Storage를 로컬 에뮬레이터에 연결한다 (dev 기본)
 */
object FirebaseSetup {
    fun initialize(emulatorHost: String?) {
        Firebase.appCheck.installAppCheckProviderFactory(appCheckProviderFactory())
        Firebase.firestore.firestoreSettings =
            firestoreSettings { setLocalCacheSettings(memoryCacheSettings {}) }
        Firebase.remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings { minimumFetchIntervalInSeconds = REMOTE_CONFIG_FETCH_INTERVAL_SECONDS },
        )
        Firebase.remoteConfig.setDefaultsAsync(RemoteFlags.DEFAULTS)
        if (emulatorHost != null) {
            Firebase.auth.useEmulator(emulatorHost, AUTH_EMULATOR_PORT)
            Firebase.firestore.useEmulator(emulatorHost, FIRESTORE_EMULATOR_PORT)
            Firebase.functions(FUNCTIONS_REGION).useEmulator(emulatorHost, FUNCTIONS_EMULATOR_PORT)
            Firebase.storage.useEmulator(emulatorHost, STORAGE_EMULATOR_PORT)
        }
        prefetchAppCheckTokenBeforeSignIn()
    }

    /**
     * 로그인 전이면 App Check 토큰을 미리 받아 둔다. 처음 받을 때 Play Integrity + 교환으로 약 2초가 걸려
     * 로그인 버튼을 누른 뒤 받으면 카카오 · 구글 로그인이 그만큼 늦어진다 (소개 · 약관 화면을 보는 동안 받는다).
     * 받은 토큰은 SDK가 저장해 만료 전까지 다시 쓰므로 추가 요청은 앱 실행당 최대 한 번이다.
     * 실패해도 무시한다: 로그인할 때 SDK가 다시 받는다.
     * 에뮬레이터 연결 뒤에 부른다 (useEmulator는 Auth를 쓰기 전에 설정해야 한다).
     */
    private fun prefetchAppCheckTokenBeforeSignIn() {
        if (Firebase.auth.currentUser != null) return
        Firebase.appCheck.getAppCheckToken(false)
    }
}
