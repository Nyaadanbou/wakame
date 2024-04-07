package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.lookup.AssetsLookup
import cc.mewcraft.wakame.pack.generate.*
import cc.mewcraft.wakame.pack.initializer.*
import cc.mewcraft.wakame.pack.service.GithubService
import cc.mewcraft.wakame.pack.service.NoneService
import cc.mewcraft.wakame.pack.service.ResourcePackService
import cc.mewcraft.wakame.pack.service.Service
import cc.mewcraft.wakame.reloadable
import cc.mewcraft.wakame.util.formatSize
import cc.mewcraft.wakame.util.writeToDirectory
import cc.mewcraft.wakame.util.writeToZipFile
import org.bukkit.entity.Player
import org.jetbrains.annotations.Blocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import team.unnamed.creative.BuiltResourcePack
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.serialize.ResourcePackReader
import team.unnamed.creative.serialize.ResourcePackWriter
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter
import team.unnamed.creative.serialize.minecraft.fs.FileTreeReader
import team.unnamed.creative.serialize.minecraft.fs.FileTreeWriter
import java.io.File

internal class ResourcePackManager(
    private val config: ResourcePackConfiguration,
    private val packReader: ResourcePackReader<FileTreeReader>,
    private val packWriter: ResourcePackWriter<FileTreeWriter>
) : KoinComponent {
    private val pluginDataDir: File by inject(named(PLUGIN_DATA_DIR))
    private val logger: Logger by inject(mode = LazyThreadSafetyMode.NONE)

    private lateinit var pack: BuiltResourcePack

    /**
     * The resource pack service.
     */
    private val service: Service by reloadable { loadService() }

    /**
     * Generates the resource pack to predefined directory.
     *
     * @return a result encapsulating whether the generation succeeds or not
     */
    fun generate(reGenerate: Boolean): Result<Unit> {
        val resourceFile = pluginDataDir.resolve(GENERATED_RESOURCE_PACK_ZIP_FILE)
        val resourcePackDir = pluginDataDir.resolve(GENERATED_RESOURCE_PACK_DIR)
        val initArg = InitializerArg(resourceFile, resourcePackDir, packReader)

        val resourcePackResult = runCatching {
            PackInitializer.chain(
                ZipPackInitializer(initArg),
                DirPackInitializer(initArg)
            ).initialize()
        }

        val isNoPack = resourcePackResult.exceptionOrNull() is NoPackException

        // Read the resource pack from the file
        // The resource pack may be empty if it's the first time to generate, so we need to handle the exception
        val resourcePack = when {
            resourcePackResult.isSuccess -> resourcePackResult.getOrThrow()
            isNoPack -> {
                logger.info("<yellow>Resource pack is empty, re-generating...")
                ResourcePack.resourcePack()
            }

            else -> return Result.failure(resourcePackResult.exceptionOrNull()!!)
        }

        if (reGenerate || isNoPack) {
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
                packWriter.writeToZipFile(resourceFile.toPath(), resourcePack)
                packWriter.writeToDirectory(resourcePackDir, resourcePack)
            }.getOrElse { return Result.failure(it) }
            logger.info("<green>Resource pack generated")
        }

        // Build the resource pack
        val builtResourcePack = runCatching { MinecraftResourcePackWriter.minecraft().build(resourcePack) }
            .getOrElse { return Result.failure(it) }
        pack = builtResourcePack
            .also { logger.info("<green>Resource pack built. File size: <yellow>${resourceFile.formatSize()}") }
        // Start the resource pack server
        runCatching { startServer(reGenerate || isNoPack) }.getOrElse { return Result.failure(it) }

        return Result.success(Unit)
    }

    private fun loadService(): Service {
        if (!::pack.isInitialized) {
            logger.error("Resource pack can not be initialized. Please check the configuration.")
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
    private fun startServer(isNoPack: Boolean) {
        if (!config.enabled) {
            logger.info("<red>Resource pack server is disabled")
            return
        }
        service.start(isNoPack)
    }

    @Blocking
    private fun stopServer() {
        service.stop()
    }
    //</editor-fold>

    fun sendToPlayer(player: Player) {
        service.sendToPlayer(player)
    }

    fun close() {
        stopServer()
    }
}