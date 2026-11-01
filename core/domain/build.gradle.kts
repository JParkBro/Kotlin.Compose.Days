plugins {
    alias(libs.plugins.days.jvm.library)
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
}
