import com.github.dockerjava.core.DockerClientBuilder
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

abstract class DockerCopyTask : DefaultTask() {

    @get:Input
    abstract val containerId: Property<String>

    @get:Input
    abstract val containerPath: Property<String>

    @get:Input
    abstract val fileMode: Property<Int>

    @get:Input
    abstract val userId: Property<Int>

    @get:Input
    abstract val groupId: Property<Int>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val sourceFile: RegularFileProperty

    @TaskAction
    fun run() {
        validateInputs()
        copyToContainer()
    }

    private fun validateInputs() {
        require(!containerId.orNull.isNullOrBlank()) { "Property 'containerId' must be specified" }
        require(!containerPath.orNull.isNullOrBlank()) { "Property 'containerPath' must be specified"}
        require(sourceFile.get().asFile.exists()) { "Source file does not exist" }
    }

    private fun copyToContainer() {
        val container = containerId.get()
        val path = containerPath.get()
        val fileMode = fileMode.get()
        val userId = userId.get()
        val groupId = groupId.get()
        val source = sourceFile.get().asFile

        logger.lifecycle("Copying JAR file {} to container: {}:{}", source.name, container, path)

        val tarOutputStream = ByteArrayOutputStream()
        TarArchiveOutputStream(tarOutputStream).use { tarOut ->
            tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)
            val entry = TarArchiveEntry(source.name).apply {
                this.size = source.length()
                this.mode = fileMode
                this.userId = userId
                this.groupId = groupId
            }

            tarOut.putArchiveEntry(entry)
            source.inputStream().use { it.copyTo(tarOut) }
            tarOut.closeArchiveEntry()
        }

        val tarInputStream = ByteArrayInputStream(tarOutputStream.toByteArray())

        // 连接到 Docker Daemon
        DockerClientBuilder.getInstance().build().use { dockerClient ->
            try {
                dockerClient.copyArchiveToContainerCmd(container)
                    .withTarInputStream(tarInputStream)
                    .withRemotePath(path)
                    .exec()
                logger.lifecycle("Successfully copied JAR file to container: {}:{}", container, path)
            } catch (e: Exception) {
                logger.error("Failed to copy JAR file to container: {}:{}", container, path, e)
                throw e
            }
        }
    }

}
