package jpark.bro.days.core.firebase

import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.functions.functions

/** Firebase Auth · Firestore · Functions 에뮬레이터 포트 (firebase/firebase.json과 같게) */
private const val AUTH_EMULATOR_PORT = 9099
private const val FIRESTORE_EMULATOR_PORT = 8080
private const val FUNCTIONS_EMULATOR_PORT = 5001

/** 함수 리전 (firebase/functions/src/index.ts의 setGlobalOptions와 같게) */
internal const val FUNCTIONS_REGION = "asia-northeast3"

/**
 * 앱 시작 시 한 번 (Application.onCreate, Koin보다 먼저). Firebase 인스턴스를 쓰기 전에 설정해야 한다.
 * - App Check: prod는 Play Integrity, dev는 디버그 제공자 (첫 실행 때 logcat의 디버그 토큰을 콘솔에 등록)
 * - Firestore 캐시는 메모리만: Room이 원본이라 디스크에 중복 저장하지 않는다 (firebase-cost-strategy.md 4장)
 * - [emulatorHost]가 있으면 Auth · Firestore · Functions를 로컬 에뮬레이터에 연결한다 (dev 기본)
 */
object FirebaseSetup {
    fun initialize(emulatorHost: String?) {
        Firebase.appCheck.installAppCheckProviderFactory(appCheckProviderFactory())
        Firebase.firestore.firestoreSettings =
            firestoreSettings { setLocalCacheSettings(memoryCacheSettings {}) }
        if (emulatorHost != null) {
            Firebase.auth.useEmulator(emulatorHost, AUTH_EMULATOR_PORT)
            Firebase.firestore.useEmulator(emulatorHost, FIRESTORE_EMULATOR_PORT)
            Firebase.functions(FUNCTIONS_REGION).useEmulator(emulatorHost, FUNCTIONS_EMULATOR_PORT)
        }
    }
}
