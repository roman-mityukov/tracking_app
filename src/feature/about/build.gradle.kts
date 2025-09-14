plugins {
    alias(libs.plugins.geo.tracking.android.feature)
    alias(libs.plugins.geo.tracking.android.library.compose)
    alias(libs.plugins.geo.tracking.hilt)
}

android {
    namespace = "io.mityukov.geo.tracking.feature.about"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:sharing"))
    implementation(libs.androidx.material3)
}