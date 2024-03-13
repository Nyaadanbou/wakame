package cc.mewcraft.wakame.pack.service

import cc.mewcraft.wakame.github.GithubRepoManager
import cc.mewcraft.wakame.pack.GENERATED_RESOURCE_PACK_DIR
import cc.mewcraft.wakame.pack.RESOURCE_PACK_ZIP_NAME
import me.lucko.helper.scheduler.HelperExecutors
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.jetbrains.annotations.Blocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import team.unnamed.creative.BuiltResourcePack
import team.unnamed.creative.server.ResourcePackServer
import java.io.File


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
    fun start(reGenerate: Boolean = false)

    /**
     * Stop the service.
     */
    fun stop()
}

data object NoneService : Service {
    override val downloadAddress = null
    override fun start(reGenerate: Boolean) = Unit // do nothing
    override fun stop() = Unit // do nothing
}

data class ResourcePackService(
    private val resourcePack: BuiltResourcePack,
    private val host: String,
    private val port: Int,
    private val appendPort: Boolean,
) : Service {
    private var server: ResourcePackServer? = null
    override var downloadAddress: String? = null

    override fun start(reGenerate: Boolean) {
        // start server
        server = ResourcePackServer.server()
            .address("0.0.0.0", port) // (required) address and port
            .pack(resourcePack) // (required) pack to serve
            .executor(HelperExecutors.asyncHelper()) // (optional) request executor (IMPORTANT!)
            .path("/get/${resourcePack.hash()}")
            .build()

        downloadAddress = if (appendPort) {
            "http://$host:$port/get/${resourcePack.hash()}/$RESOURCE_PACK_ZIP_NAME"
        } else {
            "http://$host/get/${resourcePack.hash()}/$RESOURCE_PACK_ZIP_NAME"
        }
    }

    @Blocking
    override fun stop() {
        server?.stop(0)
    }
}

data class GithubService(
    private val pluginDataDir: File,
    private val repo: String,
    private val username: String,
    private val token: String,
    private val remotePath: String,
    private val branch: String,
    private val commitMessage: String,
) : Service, KoinComponent {
    private val logger: ComponentLogger by inject(mode = LazyThreadSafetyMode.NONE)

    override var downloadAddress: String? = null // TODO: implement this

    override fun start(reGenerate: Boolean) {
        if (!reGenerate) return
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

        manager.updatePack().onFailure {
            logger.error("Failed to update resource pack", it)
            downloadAddress = null
        }
    }

    override fun stop() = Unit // do nothing
}