import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import java.net.URI

// Messenger: https://github.com/Hexaoxide/Messenger
fun RepositoryHandler.jmpSnapshots(action: MavenArtifactRepository.() -> Unit = {}): MavenArtifactRepository {
    return maven {
        name = "jmpSnapshots"
        url = URI("https://repo.jpenilla.xyz/snapshots")
        mavenContent {
            includeGroup("de.hexaoxi")
        }
        action(this)
    }
}