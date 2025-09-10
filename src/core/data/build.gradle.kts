plugins {
    alias(libs.plugins.geo.tracking.android.library)
    alias(libs.plugins.geo.tracking.hilt)
}
android {
    namespace = "io.mityukov.geo.tracking.core.data"
}

dependencies {
    implementation(project(":core:common"))
    api(project(":core:database"))
    api(project(":core:datastore"))
    implementation(project(":core:log"))
    implementation(project(":core:model"))
    implementation(libs.androidx.lifecycle.service)
}