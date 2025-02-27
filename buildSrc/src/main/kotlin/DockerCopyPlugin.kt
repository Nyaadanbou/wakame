import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

class DockerCopyPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create<DockerCopyExtension>("dockerCopy")

        project.tasks.register<DockerCopyTask>("copyJarToDocker") {
            group = "nyaadanbou"

            // 配置属性
            containerId.convention(extension.containerId)
            containerPath.convention(extension.containerPath)
            fileMode.convention(extension.fileMode)
            userId.convention(extension.userId)
            groupId.convention(extension.groupId)

            // 配置源文件
            val archiveTask = extension.archiveTask.flatMap { name ->
                project.tasks.named<AbstractArchiveTask>(name)
            }
            sourceFile.convention(archiveTask.flatMap { task -> task.archiveFile })

            // 建立依赖
            dependsOn(archiveTask)

            // 后置验证
            doFirst {
                if (containerId.get().isBlank()) {
                    throw IllegalArgumentException("dockerCopy.containerId must be specified")
                }
            }
        }

        project.afterEvaluate {
            project.configureTaskDependencies()
        }
    }
}