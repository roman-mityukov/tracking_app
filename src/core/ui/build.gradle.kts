plugins {
    alias(libs.plugins.geo.tracking.android.library)
    alias(libs.plugins.geo.tracking.android.library.compose)
}
android {
    namespace = "io.mityukov.geo.tracking.core.ui"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:test"))
    implementation(libs.androidx.material3)
}