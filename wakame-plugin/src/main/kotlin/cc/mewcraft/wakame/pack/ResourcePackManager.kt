package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.scheme.NekoItem
import cc.mewcraft.wakame.pack.generate.*
import cc.mewcraft.wakame.pack.generate.ResourcePackIconGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackMetaGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackModelGeneration
import cc.mewcraft.wakame.registry.NekoItemRegistry
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
import kotlin.math.log10
import kotlin.math.pow

private const val RESOURCE_PACK_NAME = "wakame.zip"

private const val GENERATED_RESOURCE_PACK_FILE = "generated/$RESOURCE_PACK_NAME"

@ReloadDependency(
    runBefore = [NekoItemRegistry::class]
)
internal class ResourcePackManager : Initializable, KoinComponent {
    private val pluginDataDir: File by inject(named(PLUGIN_DATA_DIR))
    private val logger: ComponentLogger by inject(mode = LazyThreadSafetyMode.NONE)
    private val plugin: WakamePlugin by inject()

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
        plugin.saveResourceRecursively(PLUGIN_ASSETS_DIR)

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
            val allItems: Set<NekoItem> = NekoItemRegistry.values

            val generationArgs = GenerationArgs(
                resourcePack = resourcePack,
                allItems = allItems
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

        if (!resourcePackPath.name.endsWith(".zip")) {
            return Result.failure(IOException("Resource pack file name must end with '.zip'"))
        }

        return Result.success(resourcePackPath)
    }

    private fun File.formatSize(): String {
        // Do not look at this code, it's not important
        if (!this.exists()) return "File does not exist"
        val size = this.length()
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
        // size.toDouble(): 首先，将文件大小（以字节为单位）转换为double类型，以便进行浮点数运算。
        // Math.log10(size.toDouble()): 然后，计算文件大小的以10为底的对数。对数的作用是将原始大小转换为一个可以比较不同数量级的值。
        // 例如，对数可以将数值范围从 1 到 1,000,000 映射到 0 到 6。
        // Math.log10(1024.0): 这是计算以1024为底的换算基准的对数值，因为文件大小的单位换算是基于1024（1KB = 1024B, 1MB = 1024KB, 等等）。
        // 除法操作：将文件大小的对数除以1024的对数，实质上是计算文件大小对应的是哪个数量级。这个结果指示了应该使用哪个单位（如B, KB, MB等）。
        // toInt(): 最后，结果转换为整数，因为单位数组units的索引必须是整数。这个整数digitGroups代表了文件大小应该使用的单位索引。
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        // 这里，文件大小除以1024的digitGroups次幂，实质上是将文件大小转换为对应单位的值。例如，如果digitGroups为1（表示KB），则文件大小除以1024，得到以KB为单位的大小。
        // %.1f: 这个格式指定符告诉String.format方法，输出的浮点数应该保留一位小数。
        // %s: 这是格式字符串的一部分，指定了一个字符串替换位，用于插入单位名称。
        return String.format("%.1f %s", size / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
    }
    //</editor-fold>

    //<editor-fold desc="Resource pack server">
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