package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.lookup.AssetsLookup
import cc.mewcraft.wakame.pack.generate.*
import cc.mewcraft.wakame.pack.service.GithubService
import cc.mewcraft.wakame.pack.service.NoneService
import cc.mewcraft.wakame.pack.service.ResourcePackService
import cc.mewcraft.wakame.pack.service.Service
import cc.mewcraft.wakame.registry.NekoItemRegistry
import cc.mewcraft.wakame.util.formatSize
import com.google.common.base.Throwables
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.Server
import org.bukkit.entity.Player
import org.jetbrains.annotations.Blocking
import org.jetbrains.annotations.Contract
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import team.unnamed.creative.BuiltResourcePack
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackReader
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter
import java.io.File
import java.io.IOException
import java.util.zip.ZipException

@ReloadDependency(
    runBefore = [NekoItemRegistry::class]
)
internal class ResourcePackManager(
    private val config: ResourcePackConfiguration,
) : Initializable, KoinComponent {
    private val pluginDataDir: File by inject(named(PLUGIN_DATA_DIR))
    private val logger: ComponentLogger by inject(mode = LazyThreadSafetyMode.NONE)

    private lateinit var pack: BuiltResourcePack

    /**
     * The resource pack service.
     */
    private lateinit var service: Service

    /**
     * Generates the resource pack to predefined directory.
     *
     * @return a result encapsulating whether the generation succeeds or not
     */
    fun generate(reGenerate: Boolean = false): Result<Unit> {
        var regen = reGenerate || !this::pack.isInitialized

        val resourceFile = initFile()
            .getOrElse { return Result.failure(it) }
        val resourcePackResult = runCatching {
            if (regen) return@runCatching ResourcePack.resourcePack()

            MinecraftResourcePackReader.minecraft()
                .readFromZipFile(resourceFile)
                .also { logger.info("Resource pack read") }
        }

        // Read the resource pack from the file
        // The resource pack may be empty if it's the first time to generate, so we need to handle the exception
        val resourcePack = when {
            resourcePackResult.isSuccess -> resourcePackResult.getOrThrow()
            reGenerate || Throwables.getRootCause(resourcePackResult.exceptionOrNull()!!) !is ZipException -> return Result.failure(resourcePackResult.exceptionOrNull()!!)
            else -> {
                logger.info("<yellow>Resource pack is empty, re-generating...".mini)
                ResourcePack.resourcePack().also { regen = true }
            }
        }

        if (regen) {
            val generationArgs = GenerationArgs(
                description = config.description,
                resourcePack = resourcePack,
                allAssets = AssetsLookup.allAssets
            )

            // Generate the resource pack
            ResourcePackGeneration.chain(
                ResourcePackMetaGeneration(generationArgs),
                ResourcePackIconGeneration(generationArgs),
                ResourcePackRegistryModelGeneration(generationArgs),
                ResourcePackCustomModelGeneration(generationArgs),
                ResourcePackExternalGeneration(generationArgs)
            ).generate().getOrElse {
                if (it !is ResourcePackExternalGeneration.GenerationCancelledException) {
                    return Result.failure(it)
                }
            }

            // Write the resource pack to the file
            runCatching {
                MinecraftResourcePackWriter.minecraft()
                    .writeToZipFile(resourceFile, resourcePack)
                MinecraftResourcePackWriter.minecraft()
                    .writeToDirectory(pluginDataDir.resolve(GENERATED_RESOURCE_PACK_DIR), resourcePack)
            }.getOrElse { return Result.failure(it) }
            logger.info("<green>Resource pack generated".mini)
        }

        // Build the resource pack
        val builtResourcePack = runCatching { MinecraftResourcePackWriter.minecraft().build(resourcePack) }
            .getOrElse { return Result.failure(it) }
        pack = builtResourcePack
            .also { logger.info("<green>Resource pack built. File size: <yellow>${resourceFile.formatSize()}".mini) }
        service = loadService()
        // Start the resource pack server
        runCatching { startServer(regen) }.getOrElse { return Result.failure(it) }

        return Result.success(Unit)
    }

    //<editor-fold desc="Init resource pack file">
    private fun initFile(): Result<File> {
        val resourcePackPath = pluginDataDir.resolve(GENERATED_RESOURCE_PACK_ZIP_FILE)
        if (resourcePackPath.isDirectory) {
            return Result.failure(IOException("Resource pack path is a directory"))
        }

        if (!resourcePackPath.exists()) {
            // Create the resource pack file if it doesn't exist
            resourcePackPath.parentFile.mkdirs()
            if (!resourcePackPath.createNewFile()) {
                return Result.failure(IOException("Failed to create resource pack file"))
            }
        }

        return Result.success(resourcePackPath)
    }
    //</editor-fold>

    private fun loadService(): Service {
        if (this::service.isInitialized) {
            stopServer()
        }

        if (!this::pack.isInitialized) {
            logger.error("Resource pack can not be initialized. Please check the configuration.", IllegalStateException("Resource pack is not initialized"))
            return NoneService
        }

        return when (val service = config.service) {
            "none" -> NoneService

            "self_host" -> {
                val host = config.host
                val port = config.port
                ResourcePackService(pack, host, port, config.appendPort)
            }

            "github" -> {
                val username = config.githubUsername
                val repo = config.githubRepo
                val token = config.githubToken
                val path = config.githubPath
                val branch = config.githubBranch
                val commitMessage = config.githubCommitMessage
                GithubService(pluginDataDir, repo, username, token, path, branch, commitMessage)
            }

            else -> {
                logger.error("Unknown resource pack service: $service", IllegalStateException("Unknown resource pack service"))
                NoneService
            }
        }.also { logger.info("Resource pack service: ${it::class.simpleName}") }
    }

    //<editor-fold desc="Resource pack server test">
    @Contract(pure = true)
    private fun startServer(reGenerate: Boolean) {
        if (!config.enabled) {
            logger.info("<red>Resource pack server is disabled".mini)
            return
        }
        service.start(reGenerate)
    }

    @Blocking
    private fun stopServer() {
        service.stop()
    }
    //</editor-fold>

    fun sendToPlayer(player: Player) {
        if (!this::pack.isInitialized) {
            player.takeIf { !it.hasPermission("wakame.admin") && it.isOnline }
                ?.let {
                    player.kick("<red>Resource pack is not ready. Please wait a moment.".mini)
                }
            return
        }
        val downloadAddress = service.downloadAddress
            ?: return

        player.setResourcePack(
            downloadAddress,
            pack.hash(),
            true,
            "<red>WA-KA-ME Resource Pack!!!!!!!!!".mini
        )
    }

    //<editor-fold desc="Initializable">
    override fun close() {
        stopServer()
    }

    override fun onReload() {
        stopServer()
        generate(true).getOrElse { logger.error("Failed to re-generate resource pack", it) } // TODO: Only for development. When the resource pack is stable, remove 'true'
        get<Server>().onlinePlayers.forEach { sendToPlayer(it) }
    }
    //</editor-fold>
}