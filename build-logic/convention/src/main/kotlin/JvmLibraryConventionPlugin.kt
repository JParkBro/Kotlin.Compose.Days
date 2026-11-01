import jpark.bro.days.convention.addUnitTestDependencies
import jpark.bro.days.convention.configureKotlinJvm
import org.gradle.api.Plugin
import org.gradle.api.Project

class JvmLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            configureKotlinJvm()
            addUnitTestDependencies()
        }
    }
}
