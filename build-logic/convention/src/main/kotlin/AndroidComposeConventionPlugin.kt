import com.android.build.api.dsl.CommonExtension
import jpark.bro.days.convention.addComposeDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

/** app · 라이브러리 공용. days.android.application 또는 days.android.library 다음에 적용한다. */
class AndroidComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.getByType<CommonExtension>().buildFeatures.compose = true

            addComposeDependencies()
        }
    }
}
