plugins {
    alias(libs.plugins.geo.tracking.android.library)
    alias(libs.plugins.geo.tracking.hilt)
}
android {
    namespace = "io.mityukov.geo.tracking.core.data.impl"
}
dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data-api"))
    api(project(":core:database"))
    api(project(":core:datastore"))
    api(project(":core:gpx"))
    implementation(project(":core:log"))
    implementation(project(":core:model"))
    implementation(libs.androidx.lifecycle.service)
}