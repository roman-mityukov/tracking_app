plugins {
    alias(libs.plugins.geo.tracking.android.library)
}

android {
    namespace = "io.mityukov.geo.tracking.core.yandexmap"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    api(libs.yandex.map.kit)
}