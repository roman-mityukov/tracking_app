plugins {
    alias(libs.plugins.geo.tracking.android.library)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "io.mityukov.geo.tracking.core.network"
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp3)
    implementation(libs.kotlinx.serialization.json)
}