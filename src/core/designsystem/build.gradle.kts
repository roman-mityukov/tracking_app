plugins {
    alias(libs.plugins.geo.tracking.android.library)
    alias(libs.plugins.geo.tracking.android.library.compose)
}
android {
    namespace = "io.mityukov.geo.tracking.core.designsystem"
}

dependencies {
    implementation(project(":core:test"))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.iconsExtended)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}