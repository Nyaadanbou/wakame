import org.gradle.api.provider.Property

/**
 * Configures docker-copy parameters.
 *
 * @property containerId Target Docker container ID (required)
 * @property containerPath Destination path in container (default: /)
 * @property archiveTask Name of task producing the archive (default: jar)
 */
abstract class DockerCopyExtension {

    abstract val containerId: Property<String>
    abstract val containerPath: Property<String>
    abstract val fileMode: Property<Int>
    abstract val userId: Property<Int>
    abstract val groupId: Property<Int>
    abstract val archiveTask: Property<String>

    init {
        fileMode.convention(0b110_100_100)
        userId.convention(0)
        groupId.convention(0)
    }

}