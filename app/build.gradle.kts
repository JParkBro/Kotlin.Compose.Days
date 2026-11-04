import java.util.Properties

plugins {
    alias(libs.plugins.days.android.application)
    alias(libs.plugins.days.android.compose)
    alias(libs.plugins.days.koin)
    // app/google-services.json (커밋하지 않음)에 dev · prod 패키지가 모두 들어 있다
    alias(libs.plugins.google.services)
}

/** local.properties (커밋하지 않음). 없으면 기본값을 쓴다 */
val localProperties =
    Properties().apply {
        rootProject
            .file("local.properties")
            .takeIf { it.exists() }
            ?.inputStream()
            ?.use(::load)
    }

android {
    namespace = "jpark.bro.days"

    productFlavors {
        // 카카오 네이티브 앱 키는 패키지별로 다르다 (module-map 8장). 없으면 빈 값: 빌드는 되고 카카오 로그인만 실패한다
        // AuthCodeHandlerActivity(카카오 계정 웹 로그인 복귀)의 스킴은 kakao{키}
        fun com.android.build.api.dsl.ApplicationProductFlavor.kakaoNativeAppKey(property: String) {
            val key = localProperties.getProperty(property, "")
            buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$key\"")
            manifestPlaceholders["kakaoScheme"] = "kakao$key"
        }
        // dev는 기본으로 로컬 Firebase 에뮬레이터에 연결한다 (dev · prod가 한 프로젝트를 써서 운영 사용량 · 예산을 지키기 위해)
        // 실제 프로젝트로 테스트할 때만 local.properties에 firebase.useEmulator=false
        // 실기기는 firebase.emulator.host=127.0.0.1 + adb reverse (에뮬레이터 기본값 10.0.2.2 = Mac의 localhost)
        named("dev") {
            val useEmulator = localProperties.getProperty("firebase.useEmulator", "true").toBoolean()
            val host = localProperties.getProperty("firebase.emulator.host", "10.0.2.2")
            buildConfigField("String", "FIREBASE_EMULATOR_HOST", if (useEmulator) "\"$host\"" else "null")
            kakaoNativeAppKey("KAKAO_NATIVE_APP_KEY_DEV")
        }
        named("prod") {
            buildConfigField("String", "FIREBASE_EMULATOR_HOST", "null")
            kakaoNativeAppKey("KAKAO_NATIVE_APP_KEY_PROD")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.navigation3)
    implementation(libs.bundles.koin.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // core
    implementation(projects.core.navigation)
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.network)
    implementation(projects.core.firebase)
    implementation(projects.core.data)
    implementation(projects.core.sync)
    implementation(projects.core.notification)
    implementation(projects.core.ads)
    implementation(projects.widget)

    // feature
    implementation(projects.feature.onboarding.api)
    implementation(projects.feature.onboarding.impl)
    implementation(projects.feature.couple.api)
    implementation(projects.feature.couple.impl)
    implementation(projects.feature.home.api)
    implementation(projects.feature.home.impl)
    implementation(projects.feature.calendar.api)
    implementation(projects.feature.calendar.impl)
    implementation(projects.feature.item.api)
    implementation(projects.feature.item.impl)
    implementation(projects.feature.notifications.api)
    implementation(projects.feature.notifications.impl)
    implementation(projects.feature.settings.api)
    implementation(projects.feature.settings.impl)
}
