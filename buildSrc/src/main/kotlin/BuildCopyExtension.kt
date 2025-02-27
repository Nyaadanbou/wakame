import org.gradle.api.provider.Property

/**
 * Configures build-copy parameters.
 *
 * @property fileName The name of the target file.
 * @property archiveTask The name of the task to copy from.
 */
abstract class BuildCopyExtension {

    abstract val fileName: Property<String>
    abstract val archiveTask: Property<String>

}