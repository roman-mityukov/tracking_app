import com.android.build.api.dsl.LibraryExtension
import io.github.takahirom.roborazzi.RoborazziExtension
import io.mityukov.android.build.convention.libsExt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "geo.tracking.android.library")
            apply(plugin = "geo.tracking.hilt")
            apply(plugin = "io.github.takahirom.roborazzi")
            apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

            extensions.configure<RoborazziExtension> {
                outputDir.set(file("src/test/screenshots"))
            }

            extensions.configure<LibraryExtension> {
                testOptions.animationsDisabled = true

                testOptions {
                    unitTests {
                        isIncludeAndroidResources = true
                    }
                }
            }

            dependencies {
                "implementation"(project(":core:ui"))
                "implementation"(project(":core:designsystem"))
                "implementation"(project(":core:test"))

                "implementation"(libsExt.findLibrary("androidx.hilt.navigation.compose").get())
                "implementation"(libsExt.findLibrary("androidx.navigation.compose").get())
                "implementation"(libsExt.findLibrary("androidx.tracing.ktx").get())
                "implementation"(libsExt.findLibrary("kotlinx.serialization.json").get())

                "debugImplementation"(
                    libsExt.findLibrary("androidx.ui.test.manifest").get(),
                )

                "testImplementation"(libsExt.findLibrary("roborazzi").get())
                "testImplementation"(libsExt.findLibrary("roborazzi.compose").get())
                "testImplementation"(libsExt.findLibrary("roborazzi.rule").get())
                "testImplementation"(libsExt.findLibrary("androidx.navigation.testing").get())
                "testImplementation"(libsExt.findLibrary("androidx.compose.bom").get())
                "testImplementation"(libsExt.findLibrary("androidx.ui.test.junit4").get())

                "androidTestImplementation"(
                    libsExt.findLibrary("androidx.compose.bom").get(),
                )
                "androidTestImplementation"(
                    libsExt.findLibrary("androidx.lifecycle.runtime.testing").get(),
                )
                "androidTestImplementation"(
                    libsExt.findLibrary("androidx.ui.test.junit4").get(),
                )
            }
        }
    }
}
