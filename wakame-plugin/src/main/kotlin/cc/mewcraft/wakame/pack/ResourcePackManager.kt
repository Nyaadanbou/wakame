package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.ConfigAccess
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.pack.generate.ResourcePackGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackGenerationContext
import cc.mewcraft.wakame.pack.generate.ResourcePackIconGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackMergePackGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackMetaGeneration
import cc.mewcraft.wakame.util.formatSize
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter
import xyz.xenondevs.commons.provider.map
import xyz.xenondevs.commons.provider.mapEach
import xyz.xenondevs.commons.provider.orElse
import java.io.File
import java.nio.file.Path

internal val RESOURCE_PACK_CONFIG = ConfigAccess.INSTANCE["resourcepack"]

private class ResourcePackGenerationSettings {
    private val config = RESOURCE_PACK_CONFIG.node("generation")

    private fun finalizePath(rawPath: Path): Path =
        if (rawPath.isAbsolute) {
            rawPath
        } else {
            val serverRoot = File("").absoluteFile.toPath()
            serverRoot.resolve(rawPath)
        }

    // output
    val outputFile: Path by config.entry<Path>("output", "file").map(::finalizePath)
    val outputDirectory: Path by config.entry<Path>("output", "directory").map(::finalizePath)

    // mcmeta
    val description: String by config.entry<String>("mcmeta", "description")
    val format: Int by config.entry<Int>("mcmeta", "format")
    val min: Int by config.entry<Int>("mcmeta", "min")
    val max: Int by config.entry<Int>("mcmeta", "max")

    // merge_packs
    val mergePacks: List<Path> by config.optionalEntry<List<Path>>("merge_packs").orElse(emptyList()).mapEach(::finalizePath)
}

internal object ResourcePackManager {
    private val settings: ResourcePackGenerationSettings = ResourcePackGenerationSettings()

    /**
     * 生成的资源包文件的路径 (ZIP 文件).
     */
    val outputFile: File
        get() = settings.outputFile.toFile()

    /**
     * 生成的资源包文件的路径 (文件夹).
     */
    val outputDirectory: File
        get() = settings.outputDirectory.toFile()

    /**
     * 基于当前所有的配置文件, 生成一个资源包, 并储存到设定好的地方.
     *
     * @return 包含生成是否成功的结果
     */
    suspend fun generate() {
        // 创建 ResourcePack 实例 (可变), 接下来将逐步修改其状态
        val resourcePack = ResourcePack.resourcePack()

        // 创建资源包生成的上下文
        val generationCtx = ResourcePackGenerationContext(
            description = settings.description,
            format = settings.format,
            min = settings.min,
            max = settings.max,
            mergePacks = settings.mergePacks,
            resourcePack = resourcePack,
        )

        // 构建 ResourcePack
        val generations: List<ResourcePackGeneration> = listOf(
            ResourcePackMetaGeneration(generationCtx),
            ResourcePackIconGeneration(generationCtx),
            ResourcePackMergePackGeneration(generationCtx),
        )

        try {
            generations.forEach { it.process() }
        } catch (t: Throwable) {
            LOGGER.warn("Failed to generate resource pack. Nothing will be written to disk.", t)
            return
        }

        // 先删除已有的文件/文件夹, 然后创建空的文件/文件夹
        outputFile.delete()
        outputFile.parentFile.mkdirs()
        outputFile.createNewFile()
        outputDirectory.deleteRecursively()
        outputDirectory.mkdirs()

        // 向磁盘写入数据
        MinecraftResourcePackWriter.minecraft().writeToZipFile(outputFile, resourcePack)
        MinecraftResourcePackWriter.minecraft().writeToDirectory(outputDirectory, resourcePack)

        LOGGER.info("Resource pack has been generated. Size: ${outputFile.formatSize()}")
    }
}
