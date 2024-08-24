@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.lookup.Assets
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.lookup.itemType
import cc.mewcraft.wakame.pack.RESOURCE_NAMESPACE
import cc.mewcraft.wakame.pack.VanillaResourcePack
import cc.mewcraft.wakame.pack.model.ModelRegistry
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.validateAssetsPathStringOrThrow
import me.lucko.helper.text3.mini
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import team.unnamed.creative.base.Readable
import team.unnamed.creative.base.Writable
import team.unnamed.creative.metadata.pack.PackFormat
import team.unnamed.creative.metadata.pack.PackMeta
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures
import team.unnamed.creative.serialize.minecraft.model.ModelSerializer
import team.unnamed.creative.texture.Texture
import team.unnamed.hephaestus.writer.ModelWriter
import java.io.File
import team.unnamed.creative.model.Model as CreativeModel

sealed class ResourcePackGeneration(
    protected val context: GenerationContext,
) {
    companion object {
        fun chain(vararg generations: ResourcePackGeneration): ResourcePackGeneration {
            generations.reduce { acc, generation ->
                acc.next = generation
                generation
            }
            return generations.first()
        }
    }

    protected var next: ResourcePackGeneration? = null

    protected fun generateNext(): Result<Unit> {
        return next?.generate() ?: Result.success(Unit)
    }

    /**
     * 生成资源包. 原来的资源包会发生变化.
     *
     * @return 封装了一个结果, 代表资源包生成是否成功
     */
    abstract fun generate(): Result<Unit>
}

internal class ResourcePackMetaGeneration(
    context: GenerationContext,
) : ResourcePackGeneration(context) {
    override fun generate(): Result<Unit> {
        try {
            val packFormat = PackFormat.format(context.format, context.min, context.max)
            val packMeta = PackMeta.of(packFormat, context.description.mini)
            context.pack.packMeta(packMeta)
        } catch (e: Throwable) {
            return Result.failure(e)
        }
        return generateNext()
    }
}

internal class ResourcePackIconGeneration(
    context: GenerationContext,
) : ResourcePackGeneration(context), KoinComponent {
    private val assetsDir: File by inject(named(PLUGIN_ASSETS_DIR))

    override fun generate(): Result<Unit> {
        try {
            context.pack.icon(Writable.file(assetsDir.resolve("logo.png")))
        } catch (e: Throwable) {
            return Result.failure(e)
        }
        return generateNext()
    }
}

internal class ResourcePackExternalGeneration(
    context: GenerationContext,
) : ResourcePackGeneration(context) {
    class GenerationCancelledException : Throwable() {
        override val message: String = "Resource pack generation is cancelled"
    }

    override fun generate(): Result<Unit> {
        // try {
        //     // TODO: 异步触发事件
        //     val isCancelled = ResourcePackGenerateEvent(args).callEvent()
        //     if (isCancelled) {
        //         return Result.failure(GenerationCancelledException())
        //     }
        // } catch (e: Throwable) {
        //     return Result.failure(e)
        // }
        return generateNext()
    }
}

internal class ResourcePackRegistryModelGeneration(
    context: GenerationContext,
) : ResourcePackGeneration(context) {
    override fun generate(): Result<Unit> {
        try {
            ModelWriter.resource(RESOURCE_NAMESPACE).write(context.pack, ModelRegistry.models())
        } catch (e: Throwable) {
            return Result.failure(e)
        }
        return generateNext()
    }
}

internal class ResourcePackCustomModelGeneration(
    context: GenerationContext,
) : ResourcePackGeneration(context), KoinComponent {
    private val logger: Logger by inject()
    private val config: ItemModelDataLookup by inject()
    private val vanillaResourcePack: VanillaResourcePack by inject()

    override fun generate(): Result<Unit> {
        val assets = context.assets

        try {
            for (asset in assets) {
                val modelFiles = asset.files.takeIf { it.isNotEmpty() } ?: continue
                for ((index, modelFile) in modelFiles.withIndex()) {
                    logger.info("Generating $index model for ${asset.key}, variant ${asset.variant}, path: $modelFile")
                    val customModelData = config.saveCustomModelData(asset.key, asset.variant)
                    val resourcePack = context.pack

                    //<editor-fold desc="Custom Model generation">
                    // Original asset from config
                    val modelKey = asset.modelKey(index + asset.variant)
                    val configModelTemplate = ModelSerializer.INSTANCE.deserialize(Readable.file(modelFile), modelKey)

                    val textureData = configModelTemplate.textures().layers()
                        .mapNotNull { it.key() }
                        .map { validateAssetsPathStringOrThrow("textures/${it.value()}.png") }
                        .map { Writable.file(it) }

                    // Texture file used by custom asset
                    val customTextures = textureData.map {
                        Texture.texture()
                            .key(asset.modelKey(index + asset.variant, "png"))
                            .data(it)
                            .build()
                    }
                    // Replace the texture key with the custom texture key
                    val configModel = configModelTemplate.toBuilder()
                        .textures(
                            ModelTextures.builder()
                                .layers(customTextures.map { ModelTexture.ofKey(it.key().removeExtension()) })
                                .build()
                        )
                        .build()
                    //</editor-fold>

                    val itemTypeKey = asset.itemTypeKey()

                    // Override for custom asset data
                    val overrideGenerator = SimpleItemOverrideGenerator(
                        ItemModelData(
                            key = modelKey,
                            material = asset.itemType,
                            index = index,
                            customModelData = customModelData
                        )
                    )

                    // Override for vanilla asset
                    val vanillaModelInCustomResourcePack = resourcePack.model(itemTypeKey)

                    val vanillaCmdOverrideBuilder = vanillaModelInCustomResourcePack?.toBuilder()
                        ?: vanillaResourcePack.model(itemTypeKey).toBuilder() // Generate the vanilla model if it doesn't exist

                    val vanillaCmdOverride = vanillaCmdOverrideBuilder
                        .addOverride(overrideGenerator.generate())
                        .build()

                    resourcePack.model(configModel.toMinecraftFormat())
                    resourcePack.model(vanillaCmdOverride)
                    logger.info("Model for ${asset.key}, variant ${asset.variant} generated. CustomModelData: $customModelData")
                    for (texture in customTextures) {
                        resourcePack.texture(texture)
                        logger.info("Texture for ${asset.key}, variant ${asset.variant} generated.")
                    }
                }

                //<editor-fold desc="Remove unused custom model data">
                // All custom model data that was used by items but the items' model path is removed
                val unusedModelCustomModelData = assets
                    .filter { it.files.isEmpty() }
                    .mapNotNull { config[it.key, it.variant] }
                val removeResult = config.removeCustomModelData(*unusedModelCustomModelData.toIntArray())

                if (removeResult) {
                    logger.info("Removed unused custom model data from items with no model path: $unusedModelCustomModelData")
                }
                //</editor-fold>
            }
        } catch (e: Throwable) {
            return Result.failure(e)
        }

        return generateNext()
    }

    private fun Assets.modelKey(order: Int, additionExtension: String = ""): Key {
        return if (additionExtension.isBlank()) {
            Key(RESOURCE_NAMESPACE, "item/${key.namespace()}/${key.value()}_$order")
        } else {
            Key(RESOURCE_NAMESPACE, "item/${key.namespace()}/${key.value()}_$order.$additionExtension")
        }
    }

    private fun Assets.itemTypeKey(): Key {
        return Key("item/${itemType.name.lowercase()}")
    }

    private fun Key.removeExtension(): Key {
        return Key(value().substringBeforeLast('.'))
    }

    private fun CreativeModel.toMinecraftFormat(): CreativeModel {
        val layers = textures().layers()
        if (layers.isEmpty()) return this
        val newTextures = layers.map {
            val key = requireNotNull(it.key()) { "Texture key is null" }
            val newKey = Key(RESOURCE_NAMESPACE, key.value())
            ModelTexture.ofKey(newKey)
        }

        return toBuilder()
            .textures(ModelTextures.builder().layers(newTextures).build())
            .build()
    }
}