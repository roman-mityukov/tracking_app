plugins {
    alias(libs.plugins.geo.tracking.android.library)
}
android {
    namespace = "io.mityukov.geo.tracking.core.data.api"
}
dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":core:model"))
}

