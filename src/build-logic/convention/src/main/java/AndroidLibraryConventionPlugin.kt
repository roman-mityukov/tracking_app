import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.mityukov.android.build.convention.configureKotlinAndroid
import io.mityukov.android.build.convention.disableUnnecessaryAndroidTests
import io.mityukov.android.build.convention.libsExt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")
            apply(plugin = "org.jetbrains.kotlin.android")
            apply(plugin = "io.gitlab.arturbosch.detekt")
            apply(plugin = "geo.tracking.lint")

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                allRules = false
                config.setFrom("../../detekt/detekt.yml")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                testOptions.targetSdk = 36
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                testOptions.animationsDisabled = true
                // The resource prefix is derived from the module name,
                // so resources inside ":core:module1" must be prefixed with "core_module1_"
                resourcePrefix =
                    path.split("""\W""".toRegex()).drop(1).distinct().joinToString(separator = "_")
                        .lowercase() + "_"
            }
            extensions.configure<LibraryAndroidComponentsExtension> {
                disableUnnecessaryAndroidTests(target)
            }
            dependencies {
                "implementation"(libsExt.findLibrary("androidx.tracing.ktx").get())
                "testImplementation"(libsExt.findLibrary("junit").get())
                "testImplementation"(libsExt.findLibrary("androidx.junit").get())
                "testImplementation"(libsExt.findLibrary("kotlinx.coroutines.test").get())
                "testImplementation"(libsExt.findLibrary("mockito.kotlin").get())
                "testImplementation"(libsExt.findLibrary("turbine").get())
                "testImplementation"(libsExt.findLibrary("robolectric").get())
                "testImplementation"(libsExt.findLibrary("strikt.core").get())
                "testImplementation"(libsExt.findLibrary("androidx.core.testing").get())
                "testImplementation"(libsExt.findLibrary("kotlin.test").get())
                "androidTestImplementation"(libsExt.findLibrary("kotlin.test").get())
                "androidTestImplementation"(
                    libsExt.findLibrary("androidx.junit").get(),
                )
                "androidTestImplementation"(
                    libsExt.findLibrary("androidx.test.runner").get(),
                )
                "androidTestImplementation"(
                    libsExt.findLibrary("mockito.kotlin").get(),
                )
                "androidTestImplementation"(
                    libsExt.findLibrary("mockito.android").get(),
                )
            }
        }
    }
}
