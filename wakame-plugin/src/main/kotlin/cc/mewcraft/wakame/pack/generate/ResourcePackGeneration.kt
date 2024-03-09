package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.lookup.Assets
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.lookup.material
import cc.mewcraft.wakame.util.validateAssetsPathStringOrThrow
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
    val allAssets: Collection<Assets>,
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
    private val config: ItemModelDataLookup by inject()

    @Suppress("UnstableApiUsage")
    override fun generate(): Result<Unit> {
        val assets = args.allAssets

        runCatching {
            for (asset in assets) {
                // TODO: Support multiple model files
                val modelFile = asset.modelFiles.firstOrNull() ?: continue

                logger.info("<aqua>Generating asset for ${asset.key}, SID ${asset.sid}... (Path: $modelFile)".mini)
                val customModelData = config.saveCustomModelData(asset.key, asset.sid)
                val resourcePack = args.resourcePack

                // Original asset from config
                val configModel = ModelSerializer.INSTANCE
                    .deserialize(Readable.file(modelFile), asset.modelKey())

                val textureData = configModel.textures().layers()
                    .mapNotNull { it.key() }
                    .map { validateAssetsPathStringOrThrow("textures/${it.value()}.png") }
                    .map { Writable.file(it) }

                // Texture file used by custom asset
                val customTextures = textureData.map {
                    Texture.texture()
                        .key(asset.modelKey("png"))
                        .data(it)
                        .build()
                }

                val materialKey = asset.materialKeys()

                // Override for custom asset data
                // TODO: Add other predicate for custom asset data
                val override = ItemOverride.of(
                    asset.modelKey(),
                    ItemPredicate.customModelData(customModelData)
                )

                // Override for vanilla asset
                val vanillaModelInResourcePack = resourcePack.model(materialKey)

                val vanillaCmdOverrideBuilder = if (vanillaModelInResourcePack != null) {
                    vanillaModelInResourcePack.toBuilder()
                } else {
                    // Model textures from the vanilla asset
                    val vanillaModelTextures = ModelTextures.builder()
                        .layers(ModelTexture.ofKey(materialKey))
                        .build()
                    CreativeModel.model()
                        .key(materialKey)
                        .parent(configModel.parent()) // Use the same parent as the original asset
                        .textures(vanillaModelTextures)
                }

                val vanillaCmdOverride = vanillaCmdOverrideBuilder
                    .addOverride(override)
                    .build()

                resourcePack.model(configModel.toMinecraftFormat())
                resourcePack.model(vanillaCmdOverride).also {
                    logger.info("<green>Model for ${asset.key}, SID ${asset.sid} generated. CustomModelData: $customModelData".mini)
                }
                customTextures.forEach {
                    resourcePack.texture(it).also {
                        logger.info("<green>Texture for ${asset.key}, SID ${asset.sid} generated.".mini)
                    }
                }
            }

            // Remove unused custom model data

            // 1. All custom model data that was used by items but the items' model path is removed
            val unUsedModelCustomModelData = assets
                .filter { it.modelFiles.isEmpty() }
                .map { config[it.key, it.sid] }
            val result1 = config.removeCustomModelData(*unUsedModelCustomModelData.toIntArray())

            if (result1) {
                logger.info("<yellow>Removed unused custom model data from items with no model path: $unUsedModelCustomModelData".mini)
            }
        }.onFailure { return Result.failure(it) }

        return generateNext()
    }

    private fun Assets.modelKey(additionExtension: String = ""): Key {
        return if (additionExtension.isBlank()) {
            Key.key(RESOURCE_NAME, "item/${key.namespace()}/${key.value()}")
        } else {
            Key.key(RESOURCE_NAME, "item/${key.namespace()}/${key.value()}.$additionExtension")
        }
    }

    private fun Assets.materialKeys(): Key {
        return Key.key("item/${material.name.lowercase()}")
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