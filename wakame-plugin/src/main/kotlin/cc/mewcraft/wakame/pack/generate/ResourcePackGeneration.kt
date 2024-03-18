package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.event.ResourcePackGeneratingEvent
import cc.mewcraft.wakame.lookup.Assets
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.lookup.material
import cc.mewcraft.wakame.pack.VanillaResourcePack
import cc.mewcraft.wakame.pack.ModelRegistry
import cc.mewcraft.wakame.util.validateAssetsPathStringOrThrow
import me.lucko.helper.text3.mini
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.jetbrains.annotations.Contract
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import team.unnamed.creative.base.Readable
import team.unnamed.creative.base.Writable
import team.unnamed.creative.metadata.pack.PackMeta
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures
import team.unnamed.creative.serialize.minecraft.model.ModelSerializer
import team.unnamed.creative.texture.Texture
import team.unnamed.hephaestus.writer.ModelWriter
import java.io.File
import team.unnamed.creative.model.Model as CreativeModel

private const val RESOURCE_NAME = "wakame"

sealed class ResourcePackGeneration(
    protected val args: GenerationArgs,
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

    /**
     * Generates the resource pack.
     *
     * The original resource pack will be changed.
     *
     * @return a result encapsulating whether the generation succeeds or not
     */
    @Contract(pure = false)
    abstract fun generate(): Result<Unit>

    protected fun generateNext(): Result<Unit> {
        return next?.generate() ?: Result.success(Unit)
    }
}

internal class ResourcePackMetaGeneration(
    args: GenerationArgs,
) : ResourcePackGeneration(args) {
    override fun generate(): Result<Unit> {
        runCatching {
            val packMeta = PackMeta.of(
                22,
                args.description.mini,
            )
            args.resourcePack.packMeta(packMeta)
        }.onFailure { return Result.failure(it) }

        return generateNext()
    }
}

internal class ResourcePackIconGeneration(
    args: GenerationArgs,
) : ResourcePackGeneration(args), KoinComponent {
    private val assetsDir: File by inject(named(PLUGIN_ASSETS_DIR))

    override fun generate(): Result<Unit> {
        runCatching { args.resourcePack.icon(Writable.file(assetsDir.resolve("logo.png"))) }
            .onFailure { return Result.failure(it) }

        return generateNext()
    }
}

internal class ResourcePackExternalGeneration(
    args: GenerationArgs,
) : ResourcePackGeneration(args) {
    class GenerationCancelledException : Throwable() {
        override val message: String = "Resource pack generation is cancelled"
    }

    override fun generate(): Result<Unit> {
        runCatching {
            val isCancelled = ResourcePackGeneratingEvent(args).callEvent()
            if (isCancelled)
                return Result.failure(GenerationCancelledException())
        }.onFailure { return Result.failure(it) }

        return generateNext()
    }
}

internal class ResourcePackRegistryModelGeneration(
    args: GenerationArgs,
) : ResourcePackGeneration(args) {
    override fun generate(): Result<Unit> {
        ModelWriter.resource(RESOURCE_NAME).write(args.resourcePack, ModelRegistry.values)
        return generateNext()
    }
}

internal class ResourcePackCustomModelGeneration(
    args: GenerationArgs,
) : ResourcePackGeneration(args), KoinComponent {
    private val logger: ComponentLogger by inject(mode = LazyThreadSafetyMode.NONE)
    private val config: ItemModelDataLookup by inject()
    private val vanillaResourcePack: VanillaResourcePack by inject()

    @Suppress("UnstableApiUsage")
    override fun generate(): Result<Unit> {
        val assets = args.allAssets

        try {
            for (asset in assets) {
                val modelFiles = asset.modelFiles.takeIf { it.isNotEmpty() } ?: continue
                for ((index, modelFile) in modelFiles.withIndex()) {
                    logger.info("<aqua>Generating $index model for ${asset.key}, SID ${asset.variant}... (Path: $modelFile)".mini)
                    val customModelData = config.saveCustomModelData(asset.key, asset.variant)
                    val resourcePack = args.resourcePack

                    //<editor-fold desc="Custom Model generation">
                    // Original asset from config
                    val modelKey = asset.modelKey(index + asset.variant)
                    val configModelTemplate = ModelSerializer.INSTANCE
                        .deserialize(Readable.file(modelFile), modelKey)

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
                        .textures(ModelTextures.builder().layers(customTextures.map { ModelTexture.ofKey(it.key().removeExtension()) }).build())
                        .build()
                    //</editor-fold>

                    val materialKey = asset.materialKey()

                    // Override for custom asset data
                    val overrideGenerator = ItemOverrideGeneratorProxy(
                        ItemModelData(
                            key = modelKey,
                            material = asset.material,
                            index = index,
                            customModelData = customModelData
                        )
                    )

                    // Override for vanilla asset
                    val vanillaModelInCustomResourcePack = resourcePack.model(materialKey)

                    val vanillaCmdOverrideBuilder = vanillaModelInCustomResourcePack?.toBuilder()
                        ?: vanillaResourcePack.model(materialKey).toBuilder() /* Generate the vanilla model if it doesn't exist */

                    val vanillaCmdOverride = vanillaCmdOverrideBuilder
                        .addOverride(overrideGenerator.generate())
                        .build()

                    resourcePack.model(configModel.toMinecraftFormat())
                    resourcePack.model(vanillaCmdOverride).also {
                        logger.info("<green>Model for ${asset.key}, SID ${asset.variant} generated. CustomModelData: $customModelData".mini)
                    }
                    customTextures.forEach {
                        resourcePack.texture(it).also {
                            logger.info("<green>Texture for ${asset.key}, SID ${asset.variant} generated.".mini)
                        }
                    }
                }

                //<editor-fold desc="Remove unused custom model data">
                // 1. All custom model data that was used by items but the items' model path is removed
                val unUsedModelCustomModelData = assets
                    .filter { it.modelFiles.isEmpty() }
                    .mapNotNull { config[it.key, it.variant] }
                val result1 = config.removeCustomModelData(*unUsedModelCustomModelData.toIntArray())

                if (result1) {
                    logger.info("<yellow>Removed unused custom model data from items with no model path: $unUsedModelCustomModelData".mini)
                }
                //</editor-fold>
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return generateNext()
    }

    private fun Assets.modelKey(order: Int, additionExtension: String = ""): Key {
        return if (additionExtension.isBlank()) {
            Key.key(RESOURCE_NAME, "item/${key.namespace()}/${key.value()}_$order")
        } else {
            Key.key(RESOURCE_NAME, "item/${key.namespace()}/${key.value()}_$order.$additionExtension")
        }
    }

    private fun Assets.materialKey(): Key {
        return Key.key("item/${material.name.lowercase()}")
    }

    private fun Key.removeExtension(): Key {
        return Key.key(namespace(), value().substringBeforeLast('.'))
    }

    private fun CreativeModel.toMinecraftFormat(): CreativeModel {
        val layers = textures().layers()
        if (layers.isEmpty()) return this
        val newTextures = layers.map {
            val key = it.key()!!
            val newKey = Key.key(RESOURCE_NAME, key.value())
            ModelTexture.ofKey(newKey)
        }

        return toBuilder()
            .textures(ModelTextures.builder().layers(newTextures).build())
            .build()
    }
}