plugins {
    alias(libs.plugins.days.android.library)
    alias(libs.plugins.days.android.compose)
}

android {
    namespace = "jpark.bro.days.core.ui"
}

dependencies {
    api(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(projects.core.designsystem)
    // 디데이 줄의 임박 강조 규칙 (isImminent)
    implementation(projects.core.domain)
    // 사진 표시 (기기에 저장된 파일만, 네트워크 모듈 없음) · 사진 선택기
    implementation(libs.coil.compose)
    implementation(libs.androidx.activity.compose)
}
