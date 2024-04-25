package cc.mewcraft.wakame.pack.service

import cc.mewcraft.wakame.github.GithubRepoManager
import cc.mewcraft.wakame.pack.GENERATED_RESOURCE_PACK_DIR
import cc.mewcraft.wakame.pack.RESOURCE_PACK_ZIP_NAME
import me.lucko.helper.text3.mini
import org.bukkit.entity.Player
import org.jetbrains.annotations.Blocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import team.unnamed.creative.BuiltResourcePack
import team.unnamed.creative.server.ResourcePackServer
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A service that provides a download address for the resource pack.
 */
sealed interface Service {
    /**
     * Get the download address of the service.
     *
     * @return the address of the service. If the service is not started, return null.
     */
    val downloadAddress: String?

    /**
     * Start the service.
     */
    fun start()

    /**
     * Stop the service.
     */
    fun stop()

    /**
     * Send the resource pack to the player.
     */
    fun sendToPlayer(player: Player)
}

/**
 * Represents a service that does nothing.
 */
data object NoneService : Service {
    override val downloadAddress = null
    override fun start() = Unit // do nothing
    override fun stop() = Unit // do nothing
    override fun sendToPlayer(player: Player) = Unit // do nothing
}

/**
 * Represents a service that serves the resource pack using a built-in server.
 */
data class ResourcePackService(
    private val resourcePack: BuiltResourcePack,
    private val host: String,
    private val port: Int,
    private val appendPort: Boolean,
) : Service, KoinComponent {
    private val logger: Logger by inject()
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        logger.info("Starting resource pack server. Port: $port")
    }

    private val server: ResourcePackServer = ResourcePackServer.server()
        .address(port) // (required) port
        .pack(resourcePack) // (required) pack to serve
        .executor(executor)
        .path("/get/${resourcePack.hash()}")
        .build()

    override val downloadAddress: String = if (appendPort) {
        "http://$host:$port/get/${resourcePack.hash()}/$RESOURCE_PACK_ZIP_NAME"
    } else {
        "http://$host/get/${resourcePack.hash()}/$RESOURCE_PACK_ZIP_NAME"
    }

    override fun start() {
        server.start()
    }

    @Blocking
    override fun stop() {
        server.stop(0)
    }

    override fun sendToPlayer(player: Player) {
        player.setResourcePack(
            downloadAddress,
            resourcePack.hash(),
            true,
            "<red>WA-KA-ME Resource Pack!!!!!!!!!".mini
        )
    }
}

/**
 * Represents a service that serves the resource pack using a Github repository.
 */
data class GithubService(
    private val pluginDataDir: File,
    private val repo: String,
    private val username: String,
    private val token: String,
    private val remotePath: String,
    private val branch: String,
    private val commitMessage: String,
) : Service, KoinComponent {
    private val logger: Logger by inject()

    override val downloadAddress: String? = null // Not supported

    override fun start() {
        logger.info("Publishing resource pack to Github")
        val manager = GithubRepoManager(
            localRepoPath = pluginDataDir.resolve("cache").resolve("repo"),
            resourcePackDirPath = pluginDataDir.resolve(GENERATED_RESOURCE_PACK_DIR),
            username = username,
            token = token,
            repo = repo,
            branch = branch,
            remotePath = remotePath,
            commitMessage = commitMessage,
        )

        manager.publishPack().onFailure {
            logger.error("Failed to publish resource pack", it)
        }
    }

    override fun stop() = Unit // do nothing

    /**
     * GithubService does not support sending the resource pack to the player.
     */
    override fun sendToPlayer(player: Player) = Unit // do nothing
}