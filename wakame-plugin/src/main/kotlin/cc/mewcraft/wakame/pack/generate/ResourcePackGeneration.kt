@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.util.text.mini
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.base.Writable
import team.unnamed.creative.metadata.pack.PackFormat
import team.unnamed.creative.metadata.pack.PackMeta
import team.unnamed.creative.resources.MergeStrategy
import team.unnamed.creative.serialize.ResourcePackReader
import team.unnamed.creative.serialize.minecraft.fs.FileTreeReader
import xyz.xenondevs.commons.collections.associateWithNotNull
import xyz.xenondevs.commons.collections.mapValuesNotNull
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import java.util.zip.ZipFile

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
    private val packReader: ResourcePackReader<FileTreeReader>,
) : ResourcePackGeneration(context) {

    override suspend fun process() {
        // TODO 允许测试环境正常运行
        val serverPluginDirectory = SERVER.pluginsFolder
        val resourcePack = context.resourcePack
        val mergePacks = context.mergePacks
            .associateWithNotNull {
                val file = serverPluginDirectory.resolve(it)
                if (!file.exists()) {
                    LOGGER.error("Merge pack not found: $it")
                    return@associateWithNotNull null
                }
                file
            }
            .mapValuesNotNull { (_, file) ->
                if (file.isDirectory) {
                    packReader.readFromDirectory(file)
                } else {
                    if (file.extension != "zip") {
                        LOGGER.error("Invalid file extension for merge pack: ${file.extension}")
                        return@mapValuesNotNull null
                    }
                    packReader.readFromZipFile(file)
                }
            }

        for ((path, mergePack) in mergePacks) {
            LOGGER.info("Merging pack... path: $path")
            resourcePack.merge(mergePack, MergeStrategy.mergeAndKeepFirstOnError())
        }
    }
}

/**
 * Reads a [ResourcePack] from a given ZIP [file][File].
 *
 * @param file The ZIP file
 * @return The read resource pack
 */
private fun ResourcePackReader<FileTreeReader>.readFromZipFile(file: File): ResourcePack {
    try {
        FileTreeReader.zip(ZipFile(file)).use { reader ->
            return read(reader)
        }
    } catch (e: IOException) {
        throw UncheckedIOException(e)
    }
}

private fun ResourcePackReader<FileTreeReader>.readFromDirectory(directory: File): ResourcePack {
    return read(FileTreeReader.directory(directory))
}