plugins {
    alias(libs.plugins.geo.tracking.android.feature)
    alias(libs.plugins.geo.tracking.android.library.compose)
    alias(libs.plugins.geo.tracking.hilt)
}

android {
    namespace = "io.mityukov.geo.tracking.feature.map"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:log"))
    implementation(project(":core:model"))
    implementation(project(":core:yandexmap"))
    implementation(project(":feature:track-capture"))
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}