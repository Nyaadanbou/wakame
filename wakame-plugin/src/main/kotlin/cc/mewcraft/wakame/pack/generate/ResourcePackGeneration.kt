package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.pack.CustomModelDataConfiguration
import cc.mewcraft.wakame.pack.Model
import cc.mewcraft.wakame.registry.NekoItemRegistry
import cc.mewcraft.wakame.util.validatePathString
import me.lucko.helper.text3.mini
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.jetbrains.annotations.Contract
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.base.Readable
import team.unnamed.creative.base.Writable
import team.unnamed.creative.metadata.Metadata
import team.unnamed.creative.metadata.animation.AnimationMeta
import team.unnamed.creative.metadata.pack.PackMeta
import team.unnamed.creative.model.ItemOverride
import team.unnamed.creative.model.ItemPredicate
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures
import team.unnamed.creative.serialize.minecraft.model.ModelSerializer
import team.unnamed.creative.texture.Texture
import java.io.File
import team.unnamed.creative.model.Model as CreativeModel

private const val RESOURCE_NAME = "wakame"

data class GenerationArgs(
    val resourcePack: ResourcePack,
    val allModels: Set<Model>,
)

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
                "<rainbow>Nyaadanbou Resource Pack".mini,
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
        runCatching {
            args.resourcePack.icon(Writable.file(assetsDir.resolve("logo.png")))
        }.onFailure { return Result.failure(it) }

        return generateNext()
    }
}

internal class ResourcePackModelGeneration(
    args: GenerationArgs,
) : ResourcePackGeneration(args), KoinComponent {
    private val logger: ComponentLogger by inject(mode = LazyThreadSafetyMode.NONE)
    private val config: CustomModelDataConfiguration by inject()

    @Suppress("UnstableApiUsage")
    override fun generate(): Result<Unit> {
        val models = args.allModels
        runCatching {
            for (model in models) {
                val modelFile = model.modelFile ?: continue

                logger.info("<aqua>Generating model for ${model.key}... (Path: $modelFile)".mini)
                val customModelData = config.saveCustomModelData(model.key)
                val resourcePack = args.resourcePack
                // Original model from config
                val originModel = ModelSerializer.INSTANCE
                    .deserialize(Readable.file(modelFile), model.modelKey())

                val textureData = originModel.textures().layers()
                    .mapNotNull { it.key() }
                    .map { validatePathString("textures/${it.value()}.png") }
                    .map { Writable.file(it) }

                // Texture file used by custom model
                val customTextures = textureData.map {
                    Texture.texture()
                        .key(model.modelKey("png"))
                        .data(it)
                        .meta(
                            Metadata.metadata()
                                .addPart(AnimationMeta.animation().height(16).build())
                                .build()
                        )
                        .build()
                }

                // Model textures from the vanilla model
                val vanillaModelTextures = ModelTextures.builder()
                    .layers(ModelTexture.ofKey(model.materialKey()))
                    .build()

                // Override for custom model data
                val override = ItemOverride.of(
                    model.modelKey(),
                    ItemPredicate.customModelData(customModelData)
                )

                // Override for vanilla model
                val vanillaCmdOverride = CreativeModel.model()
                    .key(model.materialKey())
                    .parent(originModel.parent()) // Use the same parent as the original model
                    .textures(vanillaModelTextures)
                    .addOverride(override)
                    .build()

                customTextures.forEach {
                    resourcePack.texture(it).also {
                        logger.info("<green>Texture for ${model.key} generated.".mini)
                    }
                }
                resourcePack.model(originModel.toMinecraftFormat())
                resourcePack.model(vanillaCmdOverride).also {
                    logger.info("<green>Model for ${model.key} generated. CustomModelData: $customModelData".mini)
                }
            }

            // Remove unused custom model data

            // 1. All custom model data that was used by items but the items are removed
            val unUsedItemKeys = config.customModelDataMap
                .filter { NekoItemRegistry.get(it.key) == null }
                .map { it.key }
            val result1 = config.removeCustomModelData(*unUsedItemKeys.toTypedArray())
            // 2. All custom model data that was used by items but the items' model path is removed
            val unUsedModelCustomModelData = models
                .filter { it.modelFile == null }
                .mapNotNull { config.customModelDataMap[it.key] }
            val result2 = config.removeCustomModelData(*unUsedModelCustomModelData.toIntArray())

            if (result1) {
                logger.info("<yellow>Removed unused custom model data from items: $unUsedItemKeys".mini)
            }
            if (result2) {
                logger.info("<yellow>Removed unused custom model data from items with no model path: $unUsedModelCustomModelData".mini)
            }
        }.onFailure { return Result.failure(it) }

        return generateNext()
    }

    private fun Model.modelKey(extension: String = ""): Key {
        return if (extension.isBlank()) {
            Key.key(RESOURCE_NAME, "item/${key.namespace()}/${key.value()}")
        } else {
            Key.key(RESOURCE_NAME, "item/${key.namespace()}/${key.value()}.$extension")
        }
    }

    private fun Model.materialKey(): Key {
        val materialKey = originalModelMaterial.key()
        return Key.key("item/${materialKey.value()}")
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