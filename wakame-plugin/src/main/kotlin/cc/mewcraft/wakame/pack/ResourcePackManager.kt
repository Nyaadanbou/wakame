package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.pack.generate.ResourcePackCustomModelGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackGenerationContext
import cc.mewcraft.wakame.pack.generate.ResourcePackIconGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackMergePackGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackMetaGeneration
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.formatSize
import cc.mewcraft.wakame.util.writeToDirectory
import cc.mewcraft.wakame.util.writeToZipFile
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.serialize.ResourcePackReader
import team.unnamed.creative.serialize.ResourcePackWriter
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter
import team.unnamed.creative.serialize.minecraft.fs.FileTreeReader
import team.unnamed.creative.serialize.minecraft.fs.FileTreeWriter
import xyz.xenondevs.commons.provider.orElse

internal val RESOURCE_PACK_CONFIG = Configs["resourcepack"]

internal class ResourcePackManager(
    private val packReader: ResourcePackReader<FileTreeReader>,
    private val packWriter: ResourcePackWriter<FileTreeWriter>,
) {
    private val generationSettings: ResourcePackGenerationSettings = ResourcePackGenerationSettings()

    /**
     * 基于当前所有的配置文件, 生成一个资源包, 并储存到设定好的地方.
     *
     * @return 包含生成是否成功的结果
     */
    fun generate() {
        val tempDir = KoishDataPaths.ROOT.resolve(".temp").toFile().apply { mkdirs() }
        try {
            val resourcePackFile = KoishDataPaths.ROOT.resolve(GENERATED_RESOURCE_PACK_ZIP_FILE).toFile()
            val resourcePackDirectory = KoishDataPaths.ROOT.resolve(GENERATED_RESOURCE_PACK_DIR).toFile()

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
                itemModelInfos = KoishRegistries.ITEM
                    .filter { it.id.namespace() != Identifier.MINECRAFT_NAMESPACE }
                    .map { ItemModelInfo(it.id, it.base.type.key()) }
            )

            // Generate the resource pack
            val generations: List<ResourcePackGeneration> = listOf(
                ResourcePackMetaGeneration(context),
                ResourcePackIconGeneration(context),
                ResourcePackCustomModelGeneration(context),
                ResourcePackMergePackGeneration(context, packReader),
            )

            try {
                generations.forEach { it.process() }
            } catch (t: Throwable) {
                LOGGER.warn("Failed to generate resourcepack", t)
            }

            // Write the resource pack to the file

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
}

private class ResourcePackGenerationSettings {
    private val config = RESOURCE_PACK_CONFIG.node("generation")

    val description: String by config.entry<String>("description")
    val format: Int by config.entry<Int>("format")
    val min: Int by config.entry<Int>("min")
    val max: Int by config.entry<Int>("max")
    val mergePacks: List<String> by config.optionalEntry<List<String>>("merge_packs").orElse(emptyList())
}