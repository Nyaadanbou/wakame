import net.kyori.blossom.BlossomExtension
import org.gradle.api.Project

fun BlossomExtension.configure(project: Project) {
    resources {
        property("version", project.version.toString())
        property("description", project.description ?: "")
    }
}