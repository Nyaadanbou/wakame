package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.item.scheme.*
import cc.mewcraft.wakame.item.scheme.meta.MaterialMeta
import cc.mewcraft.wakame.pack.CustomModelDataConfiguration
import cc.mewcraft.wakame.registry.NekoItemRegistry
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
import team.unnamed.creative.model.*
import team.unnamed.creative.serialize.minecraft.model.ModelSerializer
import team.unnamed.creative.texture.Texture
import java.io.File

data class GenerationArgs(
    val resourcePack: ResourcePack,
    val allItems: Set<NekoItem>,
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
        val items = args.allItems
        runCatching {
            for (nekoItem in items) {
                val modelPath = nekoItem.modelPath ?: continue
                val texturePath = nekoItem.texturePath ?: continue

                logger.info("<aqua>Generating model for ${nekoItem.key}... (Path: $modelPath)".mini)
                val customModelData = config.saveCustomModelData(nekoItem.key)
                val resourcePack = args.resourcePack
                val texture = Texture.texture()
                    .key(Key.key("wakame:item/${nekoItem.key.namespace()}/${nekoItem.key.value()}"))
                    .data(Writable.file(texturePath.toFile()))
                    .meta(
                        Metadata.metadata()
                            .addPart(AnimationMeta.animation().height(16).build())
                            .build()
                    )
                    .build()

                val originModel = ModelSerializer.INSTANCE
                    .deserialize(Readable.path(modelPath), Key.key(modelPath.fileName.toString()))

                val itemModel = Model.model()
                    .key(Key.key("wakame:item/${nekoItem.key.namespace()}/${nekoItem.key.value()}"))
                    .parent(originModel.parent())
                    .textures(
                        ModelTextures.builder().layers(
                            ModelTexture.ofKey(
                                Key.key("wakame:item/${nekoItem.key.namespace()}/${nekoItem.key.value()}")
                            )
                        ).build()
                    )
                    .build()

                val material = nekoItem.getItemMetaBy<MaterialMeta>().generate(SchemeGenerationContext())

                val modelTextures = ModelTextures.builder()
                    .layers(ModelTexture.ofKey(material.key))
                    .build()

                val override = ItemOverride.of(
                    Key.key("wakame:item/${nekoItem.key.namespace()}/${nekoItem.key.value()}"),
                    ItemPredicate.customModelData(customModelData)
                )

                val cmdModel = Model.model()
                    .key(Key.key("item/${material.key.value()}"))
                    .parent(originModel.parent())
                    .textures(modelTextures)
                    .addOverride(override)
                    .build()

                resourcePack.texture(texture).also {
                    logger.info("<green>Texture for ${nekoItem.key} generated. (Path: $texturePath)".mini)
                }
                resourcePack.model(itemModel)
                resourcePack.model(cmdModel).also {
                    logger.info("<green>Model for ${nekoItem.key} generated. CustomModelData: $customModelData".mini)
                }
            }

            // Remove unused custom model data

            // 1. All custom model data that was used by items but the items are removed
            val unUsedItemKeys = config.customModelDataMap
                .filter { NekoItemRegistry.get(it.key) == null }
                .map { it.key }
            val result1 = config.removeCustomModelData(*unUsedItemKeys.toTypedArray())
            // 2. All custom model data that was used by items but the items' model path is removed
            val unUsedModelCustomModelData = items
                .filter { it.modelPath == null }
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
}