import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI

fun RepositoryHandler.configure() {
    // Messenger: https://github.com/Hexaoxide/Messenger
    maven {
        name = "jmpSnapshots"
        url = URI("https://repo.jpenilla.xyz/snapshots")
        mavenContent {
            includeGroup("de.hexaoxi")
        }
    }
}