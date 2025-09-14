plugins {
    alias(libs.plugins.geo.tracking.android.library)
    alias(libs.plugins.geo.tracking.hilt)
}

android {
    namespace = "io.mityukov.geo.tracking.core.testing"
}

dependencies {
    implementation(libs.hilt.android.testing)
    implementation(libs.androidx.junit)
    implementation(libs.androidx.test.runner)
}