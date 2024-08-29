package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.lookup.AssetsLookup
import cc.mewcraft.wakame.pack.generate.GenerationContext
import cc.mewcraft.wakame.pack.generate.ResourcePackCustomModelGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackExternalGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackIconGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackMetaGeneration
import cc.mewcraft.wakame.pack.generate.ResourcePackRegistryModelGeneration
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
import java.io.File

internal class ResourcePackManager(
    private val packReader: ResourcePackReader<FileTreeReader>,
    private val packWriter: ResourcePackWriter<FileTreeWriter>,
) : KoinComponent {
    private val logger: Logger by inject()
    private val pluginDataDirectory: File by inject(named(PLUGIN_DATA_DIR))
    private val generationSettings: ResourcePackGenerationSettings = ResourcePackGenerationSettings()

    // /**
    //  * 已经生成的资源包.
    //  */
    // var builtResourcePack: BuiltResourcePack? = null
    //     private set

    /**
     * 基于当前所有的配置文件, 生成一个资源包, 并储存到设定好的地方.
     *
     * @return 包含生成是否成功的结果
     */
    fun generate(regenerate: Boolean): Result<Unit> {
        val resourcePackFile = pluginDataDirectory.resolve(GENERATED_RESOURCE_PACK_ZIP_FILE)
        val resourcePackDirectory = pluginDataDirectory.resolve(GENERATED_RESOURCE_PACK_DIR)
        val initializerArguments = InitializerArguments(resourcePackFile, resourcePackDirectory, packReader)

        val resourcePackResult = runCatching {
            ResourcePackInitializer.chain(
                ZipResourcePackInitializer(initializerArguments),
                DirResourcePackInitializer(initializerArguments)
            ).initialize()
        }

        val isEmptyPack = resourcePackResult.exceptionOrNull() is NoSuchResourcePackException

        // Read the resource pack from the file.
        // The resource pack may be empty if it's
        // the first time to generate.
        val resourcePack = when {
            isEmptyPack -> {
                logger.info("Resource pack is empty, regenerating ...")
                ResourcePack.resourcePack()
            }

            resourcePackResult.isSuccess -> {
                logger.info("Resource pack is ready, regenerating ...")
                resourcePackResult.getOrThrow()
            }

            else -> {
                return Result.failure(resourcePackResult.exceptionOrNull()!!)
            }
        }

        if (regenerate || isEmptyPack) {
            val context = GenerationContext(
                description = generationSettings.description,
                format = generationSettings.format,
                min = generationSettings.min,
                max = generationSettings.max,
                //
                pack = resourcePack,
                //
                assets = AssetsLookup.allAssets
            )

            // Generate the resource pack
            ResourcePackGeneration.chain(
                ResourcePackMetaGeneration(context),
                ResourcePackIconGeneration(context),
                ResourcePackRegistryModelGeneration(context),
                ResourcePackCustomModelGeneration(context),
                ResourcePackExternalGeneration(context)
            ).generate().getOrElse {
                logger.warn("Failed to generate resourcepack", it)
                if (it !is ResourcePackExternalGeneration.GenerationCancelledException) {
                    return Result.failure(it)
                }
            }

            // Write the resource pack to the file
            runCatching {
                packWriter.writeToZipFile(resourcePackFile.toPath(), resourcePack)
                packWriter.writeToDirectory(resourcePackDirectory, resourcePack)
            }.getOrElse {
                return Result.failure(it)
            }

            logger.info("Resource pack has been generated.")
        }

        // Build the resource pack
        runCatching {
            MinecraftResourcePackWriter.minecraft().build(resourcePack)
        }.getOrElse {
            return Result.failure(it)
        }/* .also {
            builtResourcePack = it
        } */

        logger.info("Resource pack has been generated. Size: ${resourcePackFile.formatSize()}")

        return Result.success(Unit)
    }
}

private class ResourcePackGenerationSettings {
    private val config = RESOURCE_PACK_CONFIG.derive("generation")

    val description by config.entry<String>("description")
    val format by config.entry<Int>("format")
    val min by config.entry<Int>("min")
    val max by config.entry<Int>("max")
}