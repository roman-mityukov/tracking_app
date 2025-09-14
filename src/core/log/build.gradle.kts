plugins {
    alias(libs.plugins.geo.tracking.android.library)
}
android {
    namespace = "io.mityukov.geo.tracking.core.log"
}
dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)
    implementation(libs.treessence)
}
