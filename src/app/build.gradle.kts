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
    alias(libs.plugins.protobuf)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.room)
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
    useKsp2 = false
}
room {
    schemaDirectory("$projectDir/schemas")
}
tracer {
    create("defaultConfig") {
        pluginToken = "HxrlPZRUlHnIFY25LM3l4KbL2B5buvYzwKZJ6wYZKqP"
        appToken = "iraH9CUhf6BvaKxHvXCwM8dTMRpffoXr4aCDovFdPyh1"
    }
}
protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
                register("kotlin") {
                    option("lite")
                }
            }
        }
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
        versionCode = 49
        versionName = "0.40.0"

        testInstrumentationRunner = "io.mityukov.geo.tracking.hilt.HiltAndroidJUnitRunner"

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
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.accompanist.permissions)
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
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.timber)
    implementation(libs.treessence)
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