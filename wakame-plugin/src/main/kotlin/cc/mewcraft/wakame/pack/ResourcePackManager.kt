package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.lookup.AssetsLookup
import cc.mewcraft.wakame.pack.generate.*
import cc.mewcraft.wakame.registry.NekoItemRegistry
import cc.mewcraft.wakame.util.formatSize
import com.google.common.base.Throwables
import me.lucko.helper.scheduler.HelperExecutors
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
import team.unnamed.creative.server.ResourcePackServer
import java.io.File
import java.io.IOException
import java.util.zip.ZipException

private const val RESOURCE_PACK_NAME = "wakame.zip"

private const val GENERATED_RESOURCE_PACK_FILE = "generated/$RESOURCE_PACK_NAME"

@ReloadDependency(
    runBefore = [NekoItemRegistry::class]
)
internal class ResourcePackManager : Initializable, KoinComponent {
    private val pluginDataDir: File by inject(named(PLUGIN_DATA_DIR))
    private val logger: ComponentLogger by inject(mode = LazyThreadSafetyMode.NONE)

    private var server: ResourcePackServer? = null

    private lateinit var pack: BuiltResourcePack
    private lateinit var downloadAddress: String

    /**
     * Generates the resource pack to predefined directory.
     *
     * @return a result encapsulating whether the generation succeeds or not
     */
    fun generate(reGenerate: Boolean = false): Result<Unit> {
        var regen = reGenerate

        val resourceFile = initFile().getOrElse { return Result.failure(it) }.also { logger.info("Resource pack path initialized") }
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
                resourcePack = resourcePack,
                allAssets = AssetsLookup.allAssets
            )

            // Generate the resource pack
            ResourcePackGeneration.chain(
                ResourcePackMetaGeneration(generationArgs),
                ResourcePackIconGeneration(generationArgs),
                ResourcePackModelGeneration(generationArgs),
            ).generate().getOrElse { return Result.failure(it) }

            // Write the resource pack to the file
            runCatching {
                MinecraftResourcePackWriter.minecraft()
                    .writeToZipFile(resourceFile, resourcePack)
            }.getOrElse { return Result.failure(it) }
            logger.info("<green>Resource pack generated".mini)
        }

        // Build the resource pack
        val builtResourcePack = runCatching { MinecraftResourcePackWriter.minecraft().build(resourcePack) }
            .getOrElse { return Result.failure(it) }
        runCatching { startServer(builtResourcePack) }.getOrElse { return Result.failure(it) }

        pack = builtResourcePack
            .also { logger.info("<green>Resource pack built. File size: <yellow>${resourceFile.formatSize()}".mini) }
        return Result.success(Unit)
    }

    //<editor-fold desc="Init resource pack file">
    private fun initFile(): Result<File> {
        val resourcePackPath = pluginDataDir.resolve(GENERATED_RESOURCE_PACK_FILE)
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

    //<editor-fold desc="Resource pack server test">
    @Contract(pure = true)
    private fun startServer(resourcePack: BuiltResourcePack) {
        val host = "localhost" // TODO: Configurable
        val port = 7270

        server = ResourcePackServer.server()
            .address(host, port) // (required) address and port
            .pack(resourcePack) // (required) pack to serve
            .executor(HelperExecutors.asyncHelper()) // (optional) request executor (IMPORTANT!)
            .path("/get/${resourcePack.hash()}")
            .build()

        downloadAddress = "http://$host:$port/get/${resourcePack.hash()}/$RESOURCE_PACK_NAME"

        server?.start().also { logger.info("Resource pack server started") }
    }

    @Blocking
    private fun stopServer() {
        server?.stop(0).also { logger.info("Resource pack server stopped") }
    }
    //</editor-fold>

    fun sendToPlayer(player: Player) {
        if (!this::pack.isInitialized || !this::downloadAddress.isInitialized) {
            player.takeIf { !it.hasPermission("wakame.admin") && it.isOnline }
                ?.let {
                    player.kick("<red>Resource pack is not ready. Please wait a moment.".mini)
                    return
                }
        }

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