plugins {
    alias(libs.plugins.geo.tracking.android.feature)
    alias(libs.plugins.geo.tracking.android.library.compose)
    alias(libs.plugins.geo.tracking.hilt)
    alias(libs.plugins.kotlinx.serialization)
}
android {
    namespace = "io.mityukov.geo.tracking.feature.track.list"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:model"))
    implementation(libs.androidx.material3)
    androidTestImplementation(project(":core:testing"))
    androidTestImplementation(libs.hilt.android.testing)
}