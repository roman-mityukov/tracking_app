import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.mityukov.android.build.convention.configureKotlinJvm
import io.mityukov.android.build.convention.libsExt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "java-library")
            apply(plugin = "org.jetbrains.kotlin.jvm")
            apply(plugin = "io.gitlab.arturbosch.detekt")
            apply(plugin = "geo.tracking.lint")

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                allRules = false
                config.setFrom("../../detekt/detekt.yml")
            }

            configureKotlinJvm()
            dependencies {
                "testImplementation"(libsExt.findLibrary("kotlin.test").get())
            }
        }
    }
}
