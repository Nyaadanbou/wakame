@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.lookup.Assets
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.lookup.itemType
import cc.mewcraft.wakame.pack.RESOURCE_NAMESPACE
import cc.mewcraft.wakame.pack.VanillaResourcePack
import cc.mewcraft.wakame.pack.entity.ModelRegistry
import cc.mewcraft.wakame.util.*
import cc.mewcraft.wakame.util.readTextAndToJson
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
import team.unnamed.creative.model.ItemOverride
import team.unnamed.creative.model.ItemPredicate
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures
import team.unnamed.creative.serialize.minecraft.metadata.MetadataSerializer
import team.unnamed.creative.serialize.minecraft.model.ModelSerializer
import team.unnamed.creative.texture.Texture
import team.unnamed.hephaestus.writer.ModelWriter
import java.io.File
import team.unnamed.creative.model.Model as CreativeModel

sealed class ResourcePackGeneration(
    protected val context: GenerationContext,
) {
    /**
     * 生成资源包. 原来的资源包会发生变化.
     *
     * @return 封装了一个结果, 代表资源包生成是否成功
     */
    abstract fun generate()
}

internal class ResourcePackMetaGeneration(
    context: GenerationContext,
) : ResourcePackGeneration(context) {
    override fun generate() {
        val packFormat = PackFormat.format(context.format, context.min, context.max)
        val packMeta = PackMeta.of(packFormat, context.description.mini)
        context.pack.packMeta(packMeta)
    }
}

internal class ResourcePackIconGeneration(
    context: GenerationContext,
) : ResourcePackGeneration(context), KoinComponent {
    private val assetsDir: File by inject(named(PLUGIN_ASSETS_DIR))

    override fun generate() {
        context.pack.icon(Writable.file(assetsDir.resolve("logo.png")))
    }
}

internal class ResourcePackExternalGeneration(
    context: GenerationContext,
) : ResourcePackGeneration(context) {
    class GenerationCancelledException : Throwable() {
        override val message: String = "Resource pack generation is cancelled"
    }

    override fun generate() {} // TODO: External generation
}

internal class ResourcePackRegistryModelGeneration(
    context: GenerationContext,
) : ResourcePackGeneration(context) {
    override fun generate() {
        ModelWriter.resource(RESOURCE_NAMESPACE).write(context.pack, ModelRegistry.models())
    }
}

internal class ResourcePackCustomModelGeneration(
    context: GenerationContext,
) : ResourcePackGeneration(context), KoinComponent {
    private val logger: Logger by inject()
    private val config: ItemModelDataLookup by inject()
    private val vanillaResourcePack: VanillaResourcePack by inject()

    override fun generate() {
        val assets = context.assets
        for (asset in assets) {
            val modelFiles = asset.files.takeIf { it.isNotEmpty() } ?: continue
            for ((index, modelFile) in modelFiles.withIndex()) {
                logger.info("Generating $index model for ${asset.key}, variant ${asset.variant}, path: $modelFile")
                val customModelData = config.saveCustomModelData(asset.key, asset.variant)
                val resourcePack = context.pack

                //<editor-fold desc="Custom Model generation">
                // Original asset from config
                val order = index + asset.variant // Order of the model
                val modelKey = asset.modelKey(order) // Key of the model
                val configModelTemplate = ModelSerializer.INSTANCE.deserialize(Readable.file(modelFile), modelKey)

                // Get all textures from the model
                val textureLayers = configModelTemplate.textures().layers()
                val particle = configModelTemplate.textures().particle()
                val variables = configModelTemplate.textures().variables()

                textureLayers.forEach { layer -> setResource(layer.key(), layer) }
                particle?.let { setResource(it.key(), particle) }
                variables.forEach { (_, value) -> setResource(value.key(), value) }

                // Get the item type key for generating the CustomModelData vanilla model
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

                resourcePack.model(configModelTemplate.toMinecraftFormat())
                resourcePack.model(vanillaCmdOverride)
                logger.info("Model for ${asset.key}, variant ${asset.variant} generated. CustomModelData: $customModelData")
            }

            //<editor-fold desc="Remove unused custom model data">
            // All custom model data that was used by items but the items' model path is removed
            val unusedModelCustomModelData = assets
                .filter { it.files.isEmpty() }
                .mapNotNull { config[it.key, it.variant] }
            val removeResult = config.removeCustomModelData(*unusedModelCustomModelData.toIntArray())

            if (removeResult) {
                logger.warn("Removed unused custom model data from items with no model path: $unusedModelCustomModelData")
            }
            //</editor-fold>
        }
    }

    private fun Assets.modelKey(order: Int): Key {
        return Key(RESOURCE_NAMESPACE, "item/${key.namespace()}/${key.value()}_$order")
    }

    private fun Assets.itemTypeKey(): Key {
        return Key("item/${itemType.name.lowercase()}")
    }

    private fun setResource(originKey: Key?, model: ModelTexture?) {
        model ?: return
        requireNotNull(originKey) { "Origin key must not be null" }
        val textureFile = validateAssetsPathStringOrThrow("textures/${originKey.value()}.png")
        val textureWritable = Writable.file(textureFile)

        val texture = Texture.texture()
            .key(originKey.namespace { RESOURCE_NAMESPACE }.value { "$it.png" })
            .data(textureWritable)

        val metaFile = textureFile.resolveSibling("${textureFile.name}.mcmeta").takeIf { it.exists() }
        val meta = metaFile?.let { MetadataSerializer.INSTANCE.readFromTree(it.readTextAndToJson()) }
        if (meta != null) {
            texture.meta(meta)
        }

        logger.info("Texture for $originKey generated.")

        context.pack.texture(texture.build())
    }

    /**
     * 将 Wakame 的 Key 转换为 Minecraft 的 Key
     *
     * 如 `(minecraft:)item/iron_sword_0` 转换为 `wakame:item/iron_sword_0`
     */
    private fun CreativeModel.toMinecraftFormat(): CreativeModel {
        val newLayers = textures().layers().map {
            val oldKey = it.key()
            val newKey = oldKey!!.namespace { RESOURCE_NAMESPACE }
            ModelTexture.ofKey(newKey)
        }

        val newParticle = textures().particle()?.let {
            val oldKey = it.key()
            val newKey = oldKey!!.namespace { RESOURCE_NAMESPACE }
            ModelTexture.ofKey(newKey)
        }

        val newVariables = textures().variables().map { (name, value) ->
            val oldKey = value.key()
            val newKey = oldKey!!.namespace { RESOURCE_NAMESPACE }
            name to ModelTexture.ofKey(newKey)
        }.toMap()

        return toBuilder()
            .textures(
                ModelTextures.builder()
                    .layers(newLayers)
                    .particle(newParticle)
                    .variables(newVariables)
                    .build()
            )
            .build()
    }
}

internal class ResourcePackModelSortGeneration(
    context: GenerationContext,
) : ResourcePackGeneration(context) {
    override fun generate() {
        val pack = context.pack
        for (model in pack.models()) {
            if (model.key().namespace() != Key.MINECRAFT_NAMESPACE) {
                continue
            }
            val newModelBuilder = model.toBuilder()
            val overrides = model.overrides()
            val sortedOverrides = overrides.sortedBy { it.customModelData() }
            newModelBuilder.overrides(sortedOverrides)
            pack.model(newModelBuilder.build())
        }
    }

    private fun ItemOverride.customModelData(): Int? {
        val predicates = predicate()
        val customModelData = predicates.find { it.name() == "custom_model_data" }
        return customModelData?.value() as? Int
    }
}