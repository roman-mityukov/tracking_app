plugins {
    alias(libs.plugins.geo.tracking.android.feature)
    alias(libs.plugins.geo.tracking.android.library.compose)
    alias(libs.plugins.geo.tracking.hilt)
    alias(libs.plugins.kotlinx.serialization)
}
android {
    namespace = "io.mityukov.geo.tracking.feature.track.details"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:sharing"))
    implementation(project(":core:yandexmap"))
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
