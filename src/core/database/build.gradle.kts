plugins {
    alias(libs.plugins.geo.tracking.android.library)
    alias(libs.plugins.geo.tracking.hilt)
    alias(libs.plugins.geo.tracking.android.room)
}
android {
    namespace = "io.mityukov.geo.tracking.core.database"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        getByName("test").assets.srcDir(files("$projectDir/schemas"))
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
}