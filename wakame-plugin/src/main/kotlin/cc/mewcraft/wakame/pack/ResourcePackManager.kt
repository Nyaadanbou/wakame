package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.BootstrapContexts
import cc.mewcraft.wakame.KoishDataPaths
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
import team.unnamed.creative.serialize.ResourcePackWriter
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackReader
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter
import team.unnamed.creative.serialize.minecraft.fs.FileTreeWriter
import xyz.xenondevs.commons.provider.orElse
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Path
import java.util.zip.ZipOutputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.moveTo
import kotlin.io.path.outputStream

internal val RESOURCE_PACK_CONFIG = ConfigAccess.INSTANCE["resourcepack"]

internal object ResourcePackManager {
    internal val ZIP_FILE = "${BootstrapContexts.PLUGIN_NAME}.zip"
    internal val GENERATED_DIR = "generated/${BootstrapContexts.PLUGIN_NAME}"
    internal val GENERATED_FILE = "generated/$ZIP_FILE"

    private val generationSettings: ResourcePackGenerationSettings = ResourcePackGenerationSettings()

    /**
     * 基于当前所有的配置文件, 生成一个资源包, 并储存到设定好的地方.
     *
     * @return 包含生成是否成功的结果
     */
    suspend fun generate() {
        val tempDir = KoishDataPaths.ROOT.resolve(".temp").toFile().apply { mkdirs() }
        try {
            val resourcePackFile = KoishDataPaths.ROOT.resolve(GENERATED_FILE).toFile()
            val resourcePackDirectory = KoishDataPaths.ROOT.resolve(GENERATED_DIR).toFile()

            resourcePackFile.delete()
            resourcePackDirectory.deleteRecursively()

            val resourceTempFile = tempDir.resolve(GENERATED_FILE)
            val resourceTempDir = tempDir.resolve(GENERATED_DIR)

            val resourcePack = ResourcePack.resourcePack()
            val context = ResourcePackGenerationContext(
                description = generationSettings.description,
                format = generationSettings.format,
                min = generationSettings.min,
                max = generationSettings.max,
                mergePacks = generationSettings.mergePacks,
                resourcePack = resourcePack,
            )

            // Generate the resource pack
            val generations: List<ResourcePackGeneration> = listOf(
                ResourcePackMetaGeneration(context),
                ResourcePackIconGeneration(context),
                ResourcePackMergePackGeneration(context, MinecraftResourcePackReader.minecraft()),
            )

            try {
                generations.forEach { it.process() }
            } catch (t: Throwable) {
                LOGGER.warn("Failed to generate resourcepack", t)
            }

            // Write the resource pack to the file

            val packWriter = MinecraftResourcePackWriter.minecraft()
            packWriter.writeToZipFile(path = resourcePackFile.toPath(), tempPath = resourceTempFile.toPath(), resourcePack = resourcePack)
            packWriter.writeToDirectory(directory = resourcePackDirectory, tempDir = resourceTempDir, resourcePack = resourcePack)

            LOGGER.info("Resource pack has been generated.")
            // Build the resource pack to ensure it's valid
            MinecraftResourcePackWriter.minecraft().build(resourcePack)

            LOGGER.info("Resource pack has been generated. Size: ${resourcePackFile.formatSize()}")
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun ResourcePackWriter<FileTreeWriter>.writeToZipFile(
        path: Path,
        tempPath: Path = path,
        resourcePack: ResourcePack,
    ) {
        try {
            if (!tempPath.exists()) {
                tempPath.parent.createDirectories()
                tempPath.createFile()
            }
            ZipOutputStream(tempPath.outputStream().buffered()).use { outputStream ->
                write(FileTreeWriter.zip(outputStream), resourcePack)
            }
            // Move the temporary file to the target path
            tempPath.moveTo(path, true)
        } catch (e: FileNotFoundException) {
            throw IllegalStateException("Failed to write resource pack to zip file: File not found: $path", e)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    private fun ResourcePackWriter<FileTreeWriter>.writeToDirectory(
        directory: File,
        tempDir: File = directory,
        resourcePack: ResourcePack,
    ) {
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        write(FileTreeWriter.directory(tempDir), resourcePack)
        // Move the temporary directory to the target directory
        tempDir.toPath().moveTo(directory.toPath(), true)
    }
}

private class ResourcePackGenerationSettings {
    private val config = RESOURCE_PACK_CONFIG.node("generation")

    val description: String by config.entry<String>("description")
    val format: Int by config.entry<Int>("format")
    val min: Int by config.entry<Int>("min")
    val max: Int by config.entry<Int>("max")
    val mergePacks: List<String> by config.optionalEntry<List<String>>("merge_packs").orElse(emptyList())
}