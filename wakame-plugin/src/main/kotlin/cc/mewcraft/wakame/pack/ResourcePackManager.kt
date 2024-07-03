package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.lookup.AssetsLookup
import cc.mewcraft.wakame.pack.generate.GenerationContext
import cc.mewcraft.wakame.pack.generate.ResourcePackCustomModelGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackExternalGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackIconGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackMetaGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackRegistryModelGeneration
import cc.mewcraft.wakame.pack.initializer.DirPackInitializer
import cc.mewcraft.wakame.pack.initializer.InitializerArguments
import cc.mewcraft.wakame.pack.initializer.NoSuchResourcePackException
import cc.mewcraft.wakame.pack.initializer.PackInitializer
import cc.mewcraft.wakame.pack.initializer.ZipPackInitializer
import cc.mewcraft.wakame.pack.service.GithubService
import cc.mewcraft.wakame.pack.service.NoneService
import cc.mewcraft.wakame.pack.service.ResourcePackService
import cc.mewcraft.wakame.pack.service.Service
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
    private val packWriter: ResourcePackWriter<FileTreeWriter>,
) : KoinComponent {
    private val logger: Logger by inject()
    private val pluginDataDir: File by inject(named(PLUGIN_DATA_DIR))

    private lateinit var pack: BuiltResourcePack

    /**
     * The resource pack service.
     */
    private val service: Service by ReloadableProperty { loadService() }

    /**
     * Generates the resource pack to predefined directory.
     *
     * @return a result encapsulating whether the generation succeeds or not
     */
    fun generate(regenerate: Boolean): Result<Unit> {
        val resourceFile = pluginDataDir.resolve(GENERATED_RESOURCE_PACK_ZIP_FILE)
        val resourcePackDir = pluginDataDir.resolve(GENERATED_RESOURCE_PACK_DIR)
        val initArg = InitializerArguments(resourceFile, resourcePackDir, packReader)

        val resourcePackResult = runCatching {
            PackInitializer.chain(
                ZipPackInitializer(initArg),
                DirPackInitializer(initArg)
            ).initialize()
        }

        val isNoPack = resourcePackResult.exceptionOrNull() is NoSuchResourcePackException

        // Read the resource pack from the file
        // The resource pack may be empty if it's the first time to generate, so we need to handle the exception
        val resourcePack = when {
            resourcePackResult.isSuccess -> resourcePackResult.getOrThrow()
            isNoPack -> {
                logger.info("Resource pack is empty, regenerating...")
                ResourcePack.resourcePack()
            }

            else -> return Result.failure(resourcePackResult.exceptionOrNull()!!)
        }

        if (regenerate || isNoPack) {
            val generationContext = GenerationContext(
                description = config.description,
                resourcePack = resourcePack,
                assets = AssetsLookup.allAssets
            )

            // Generate the resource pack
            ResourcePackGeneration.chain(
                ResourcePackMetaGeneration(generationContext),
                ResourcePackIconGeneration(generationContext),
                ResourcePackRegistryModelGeneration(generationContext),
                ResourcePackCustomModelGeneration(generationContext),
                ResourcePackExternalGeneration(generationContext)
            ).generate().getOrElse {
                if (it !is ResourcePackExternalGeneration.GenerationCancelledException) {
                    return Result.failure(it)
                }
            }

            // Write the resource pack to the file
            runCatching {
                packWriter.writeToZipFile(resourceFile.toPath(), resourcePack)
                packWriter.writeToDirectory(resourcePackDir, resourcePack)
            }.getOrElse {
                return Result.failure(it)
            }
            logger.info("Resource pack generated")
        }

        // Build the resource pack
        val builtResourcePack = runCatching {
            MinecraftResourcePackWriter.minecraft().build(resourcePack)
        }.getOrElse {
            return Result.failure(it)
        }
        pack = builtResourcePack
        logger.info("Resource pack built. File size: ${resourceFile.formatSize()}")

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
        }.also {
            logger.info("Resource pack service: ${it::class.simpleName}")
        }
    }

    //<editor-fold desc="Resource pack server test">
    @Blocking
    fun startServer() {
        if (!config.enabled) {
            logger.info("<red>Resource pack server is disabled")
            return
        }
        service.start()
    }

    @Blocking
    fun stopServer() {
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