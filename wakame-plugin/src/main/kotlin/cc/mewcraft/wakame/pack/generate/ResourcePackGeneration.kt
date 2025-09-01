@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.util.text.mini
import team.unnamed.creative.base.Writable
import team.unnamed.creative.metadata.pack.PackFormat
import team.unnamed.creative.metadata.pack.PackMeta
import team.unnamed.creative.resources.MergeStrategy
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackReader
import xyz.xenondevs.commons.collections.filterValuesNotNull
import kotlin.io.path.absolutePathString

/**
 * 封装了一个独立的资源包生成逻辑.
 *
 * 本类的每个实现仅代表*一部分*资源包生成的逻辑, 以实现更好的解耦.
 * 实现类可以访问 [context] ([ResourcePackGenerationContext]) 来读取当前的状态,
 * 并选择性的更新最终要生成的资源包实例 [team.unnamed.creative.ResourcePack].
 *
 * 如果 [process] 抛出异常, 将会被外部捕获并记录.
 */
sealed class ResourcePackGeneration(
    protected val context: ResourcePackGenerationContext,
) {
    /**
     * 执行资源包的生成逻辑, 更新 [context].
     */
    abstract suspend fun process()
}

internal class ResourcePackMetaGeneration(
    context: ResourcePackGenerationContext,
) : ResourcePackGeneration(context) {
    override suspend fun process() {
        val packFormat = PackFormat.format(context.format, context.min, context.max)
        val packMeta = PackMeta.of(packFormat, context.description.mini)
        context.resourcePack.packMeta(packMeta)
    }
}

internal class ResourcePackIconGeneration(
    context: ResourcePackGenerationContext,
) : ResourcePackGeneration(context) {

    override suspend fun process() {
        val icon = KoishDataPaths.ASSETS.resolve("logo.png").toFile()
        if (!icon.exists()) {
            return
        }
        context.resourcePack.icon(Writable.file(icon))
    }
}

internal class ResourcePackMergePackGeneration(
    context: ResourcePackGenerationContext,
) : ResourcePackGeneration(context) {

    override suspend fun process() {
        val resourcePack = context.resourcePack
        val mergePackByPath = context.mergePacks
            .filter { path ->
                val file = path.toFile()
                file.exists().also { exists ->
                    if (!exists) LOGGER.error("Merge pack not found: ${path.absolutePathString()}")
                }
            }
            .associate { path ->
                try {
                    val file = path.toFile()
                    if (file.isDirectory) {
                        path to MinecraftResourcePackReader.minecraft().readFromDirectory(file)
                    } else {
                        path to MinecraftResourcePackReader.minecraft().readFromZipFile(path)
                    }
                } catch (e: Exception) {
                    LOGGER.error("Failed to read merge pack: ${path.absolutePathString()}", e)
                    path to null
                }
            }
            .filterValuesNotNull()

        for ((packPath, mergePack) in mergePackByPath) {
            LOGGER.info("Merging pack: $packPath")
            resourcePack.merge(mergePack, MergeStrategy.mergeAndKeepFirstOnError())
        }
    }
}
