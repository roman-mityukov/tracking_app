@file:OptIn(KspExperimental::class)

import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.google.devtools.ksp.KspExperimental

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.apptracer)
    alias(libs.plugins.detekt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.roborazzi)
}
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("../detekt/detekt.yml")
}
hilt {
    enableAggregatingTask = false
}
ksp {
    useKsp2 = true
}
tracer {
    create("defaultConfig") {
        pluginToken = "HxrlPZRUlHnIFY25LM3l4KbL2B5buvYzwKZJ6wYZKqP"
        appToken = "iraH9CUhf6BvaKxHvXCwM8dTMRpffoXr4aCDovFdPyh1"
    }
}
roborazzi {
    outputDir.set(file("src/test/screenshots"))
}

android {
    namespace = "io.mityukov.geo.tracking"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.mityukov.geo.tracking"
        minSdk = 29
        targetSdk = 36
        versionCode = 59
        versionName = "0.48.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val yandexMapKitKey = "YANDEX_MAPKIT_API_KEY"
        buildConfigField(
            type = "String",
            name = yandexMapKitKey,
            value = gradleLocalProperties(rootDir, providers).getProperty(yandexMapKitKey)
        )
    }

    signingConfigs {
        val propsStorePassword =
            gradleLocalProperties(rootDir, providers).getProperty("STORE_PASSWORD")
        val propsKeyAlias =
            gradleLocalProperties(rootDir, providers).getProperty("KEY_ALIAS")
        val propsKeyPassword =
            gradleLocalProperties(rootDir, providers).getProperty("KEY_PASSWORD")
        getByName("debug") {
            storeFile = file("../user.keystore")
            storePassword = propsStorePassword
            keyAlias = propsKeyAlias
            keyPassword = propsKeyPassword
        }
        create("release") {
            storeFile = file("../user.keystore")
            storePassword = propsStorePassword
            keyAlias = propsKeyAlias
            keyPassword = propsKeyPassword
        }
    }
    buildTypes {
        debug {
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                //noinspection ChromeOsAbiSupport
                abiFilters += listOf("arm64-v8a")
            }
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:log"))
    implementation(project(":core:model"))
    implementation(project(":core:sharing"))
    implementation(project(":core:test"))
    implementation(project(":core:ui"))
    implementation(project(":feature:about"))
    implementation(project(":feature:map"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:splash"))
    implementation(project(":feature:track-capture"))
    implementation(project(":feature:track-details"))
    implementation(project(":feature:track-list"))
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material3)
    implementation(platform(libs.apptracer.tracer.platform))
    implementation(libs.apptracer.crash.report)
    implementation(libs.apptracer.crash.report.native)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.yandex.map.kit)
    debugImplementation(libs.leak.canary)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.strikt.core)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.rule)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.lifecycle.runtime.testing)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)
    kspAndroidTest(libs.hilt.android.compiler)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(kotlin("test"))
}