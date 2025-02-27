import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.withType

fun Project.configureTaskDependencies() {
    configurations.forEach { configuration ->
        configuration.dependencies.withType<ProjectDependency>().forEach { dep ->
            val dependentProject = dep.dependencyProject
            val copyTask = dependentProject.tasks.findByName("copyJarToBuild")
            if (copyTask != null) {
                tasks.named("compileJava") {
                    mustRunAfter(copyTask)
                }
            }
        }
    }
}