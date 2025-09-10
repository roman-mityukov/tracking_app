plugins {
    alias(libs.plugins.geo.tracking.android.library)
    alias(libs.plugins.geo.tracking.hilt)
}
android {
    namespace = "io.mityukov.geo.tracking.core.sharing"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:log"))
    implementation(project(":core:model"))
}