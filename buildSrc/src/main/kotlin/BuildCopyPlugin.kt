import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

class BuildCopyPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create<BuildCopyExtension>("buildCopy")

        project.tasks.register<BuildCopyTask>("copyJarToBuild") {
            this.group = "nyaadanbou"

            val archiveTask = extension.archiveTask.flatMap { archiveTaskName ->
                project.tasks.named<AbstractArchiveTask>(archiveTaskName)
            }
            val fileName = extension.fileName.flatMap { name ->
                if (name.isNullOrBlank()) {
                    archiveTask.flatMap { it.archiveFileName }
                } else {
                    project.provider { name }
                }
            }

            dependsOn(archiveTask)

            from(archiveTask)
            into(project.layout.buildDirectory)
            rename { fileName.get() }
        }

        project.afterEvaluate {
            project.configureTaskDependencies()
        }
    }

}