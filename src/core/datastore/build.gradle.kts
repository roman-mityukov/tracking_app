plugins {
    alias(libs.plugins.geo.tracking.android.library)
    alias(libs.plugins.geo.tracking.hilt)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "io.mityukov.geo.tracking.core.datastore"
}

dependencies {
    api(project(":core:datastore-proto"))
    api(libs.androidx.datastore)
}