package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.pack.generate.*
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.formatSize
import cc.mewcraft.wakame.util.writeToDirectory
import cc.mewcraft.wakame.util.writeToZipFile
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.serialize.ResourcePackReader
import team.unnamed.creative.serialize.ResourcePackWriter
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter
import team.unnamed.creative.serialize.minecraft.fs.FileTreeReader
import team.unnamed.creative.serialize.minecraft.fs.FileTreeWriter
import xyz.xenondevs.commons.provider.immutable.orElse
import java.io.File

internal class ResourcePackManager(
    private val packReader: ResourcePackReader<FileTreeReader>,
    private val packWriter: ResourcePackWriter<FileTreeWriter>,
) : KoinComponent {
    private val logger: Logger by inject()
    private val pluginDataDirectory: File by inject(named(PLUGIN_DATA_DIR))
    private val generationSettings: ResourcePackGenerationSettings = ResourcePackGenerationSettings()

    /**
     * 基于当前所有的配置文件, 生成一个资源包, 并储存到设定好的地方.
     *
     * @return 包含生成是否成功的结果
     */
    fun generate() {
        val tempDir = pluginDataDirectory.resolve("temp").apply { mkdirs() }
        try {
            val resourcePackFile = pluginDataDirectory.resolve(GENERATED_RESOURCE_PACK_ZIP_FILE)
            val resourcePackDirectory = pluginDataDirectory.resolve(GENERATED_RESOURCE_PACK_DIR)

            resourcePackFile.delete()
            resourcePackDirectory.deleteRecursively()

            val resourceTempFile = tempDir.resolve(GENERATED_RESOURCE_PACK_ZIP_FILE)
            val resourceTempDir = tempDir.resolve(GENERATED_RESOURCE_PACK_DIR)

            val resourcePack = ResourcePack.resourcePack()
            val context = ResourcePackGenerationContext(
                description = generationSettings.description,
                format = generationSettings.format,
                min = generationSettings.min,
                max = generationSettings.max,
                mergePacks = generationSettings.mergePacks,
                resourcePack = resourcePack,
                itemIds = ItemRegistry.CUSTOM.values.map { it.id }
            )

            // Generate the resource pack
            val generations: List<ResourcePackGeneration> = listOf(
                ResourcePackMetaGeneration(context),
                ResourcePackIconGeneration(context),
                ResourcePackRegistryModelGeneration(context),
                ResourcePackCustomModelGeneration(context),
                ResourcePackMergePackGeneration(context, packReader),
                ResourcePackModelSortGeneration(context)
            )

            try {
                generations.forEach { it.process() }
            } catch (t: Throwable) {
                logger.warn("Failed to generate resourcepack", t)
            }

            // Write the resource pack to the file

            packWriter.writeToZipFile(path = resourcePackFile.toPath(), tempPath = resourceTempFile.toPath(), resourcePack = resourcePack)
            packWriter.writeToDirectory(directory = resourcePackDirectory, tempDir = resourceTempDir, resourcePack = resourcePack)

            logger.info("Resource pack has been generated.")
            // Build the resource pack to ensure it's valid
            MinecraftResourcePackWriter.minecraft().build(resourcePack)

            logger.info("Resource pack has been generated. Size: ${resourcePackFile.formatSize()}")
        } finally {
            tempDir.deleteRecursively()
        }
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