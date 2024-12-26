@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.lookup.*
import cc.mewcraft.wakame.pack.RESOURCE_NAMESPACE
import cc.mewcraft.wakame.pack.entity.ModelRegistry
import cc.mewcraft.wakame.util.*
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
import team.unnamed.creative.model.*
import team.unnamed.creative.resources.MergeStrategy
import team.unnamed.creative.serialize.ResourcePackReader
import team.unnamed.creative.serialize.minecraft.fs.FileTreeReader
import team.unnamed.creative.serialize.minecraft.metadata.MetadataSerializer
import team.unnamed.creative.serialize.minecraft.model.ModelSerializer
import team.unnamed.creative.texture.Texture
import team.unnamed.hephaestus.writer.ModelWriter
import java.io.File
import team.unnamed.creative.model.Model as CreativeModel

/**
 * 封装了一个独立的资源包生成逻辑.
 *
 * 本类的每个实现仅代表*一部分*资源包生成的逻辑, 以实现更好的解耦.
 * 实现类可以访问 [context] ([ResourcePackGenerationContext]) 来读取当前的状态,
 * 并选择性的更新最终要生成的资源包实例 [team.unnamed.creative.ResourcePack].
 *
 * 如果 [process] 抛出异常, 将会被外部捕获并记录.
 */
sealed class ResourcePackGeneration(
    protected val context: ResourcePackGenerationContext,
) {
    /**
     * 执行资源包的生成逻辑, 更新 [context].
     */
    abstract fun process()
}

internal class ResourcePackMetaGeneration(
    context: ResourcePackGenerationContext,
) : ResourcePackGeneration(context) {
    override fun process() {
        val packFormat = PackFormat.format(context.format, context.min, context.max)
        val packMeta = PackMeta.of(packFormat, context.description.mini)
        context.resourcePack.packMeta(packMeta)
    }
}

internal class ResourcePackIconGeneration(
    context: ResourcePackGenerationContext,
) : ResourcePackGeneration(context), KoinComponent {
    private val assetsDir: File by inject(named(PLUGIN_ASSETS_DIR))

    override fun process() {
        val icon = assetsDir.resolve("logo.png")
        if (!icon.exists()) {
            return
        }
        context.resourcePack.icon(Writable.file(icon))
    }
}

internal class ResourcePackExternalGeneration(
    context: ResourcePackGenerationContext,
) : ResourcePackGeneration(context) {
    class GenerationCancelledException : Throwable() {
        override val message: String = "Resource pack generation is cancelled"
    }

    override fun process() {} // TODO: External generation
}

internal class ResourcePackRegistryModelGeneration(
    context: ResourcePackGenerationContext,
) : ResourcePackGeneration(context) {
    override fun process() {
        ModelWriter.resource(RESOURCE_NAMESPACE).write(context.resourcePack, ModelRegistry.models())
    }
}

internal class ResourcePackCustomModelGeneration(
    context: ResourcePackGenerationContext,
) : ResourcePackGeneration(context), KoinComponent {
    private val logger: Logger by inject()

    override fun process() {
        val assets = context.assets
        val resourcePack = context.resourcePack

        for (asset in assets) {
            val modelFiles = asset.files.takeIf { it.isNotEmpty() } ?: continue
            for ((index, modelFile) in modelFiles.withIndex()) {
                logger.info("Generating $index model for ${asset.itemId}, variant ${asset.variant}, path: $modelFile")

                //<editor-fold desc="Custom Model generation">
                // Original asset from config
                val modelKey = asset.modelKey()
                val configModelTemplate = ModelSerializer.INSTANCE.deserialize(Readable.file(modelFile), modelKey)

                // Get all textures from the model
                val textureLayers = configModelTemplate.textures().layers()
                val particle = configModelTemplate.textures().particle()
                val variables = configModelTemplate.textures().variables()

                textureLayers.forEach { layer -> setTexture(layer.key()) }
                particle?.let { setTexture(it.key()) }
                variables.forEach { (_, value) -> setTexture(value.key()) }

                resourcePack.model(configModelTemplate.toMinecraftFormat())
                logger.info("Model for ${asset.itemId}, variant ${asset.variant} generated.")
            }
        }
    }

    /**
     * 生成一个纹理并添加到资源包中.
     *
     * @param originTextureKey 原始配置文件中纹理的 Key. 如果为 null, 则不会生成纹理.
     */
    private fun setTexture(originTextureKey: Key?) {
        requireNotNull(originTextureKey) { "Origin key must not be null" }
        val textureFile = AssetUtils.getFileOrThrow("textures/${originTextureKey.value()}", "png")
        val textureWritable = Writable.file(textureFile)

        val texture = Texture.texture()
            .key(originTextureKey.namespace { RESOURCE_NAMESPACE }.value { "$it.png" })
            .data(textureWritable)

        val metaFile = textureFile.resolveSibling("${textureFile.name}.mcmeta").takeIf { it.exists() }
        val meta = metaFile?.let { MetadataSerializer.INSTANCE.readFromTree(AssetUtils.toJsonElement(it)) }
        if (meta != null) {
            texture.meta(meta)
        }

        logger.info("Texture for $originTextureKey generated.")

        context.resourcePack.texture(texture.build())
    }

    /**
     * 将 Wakame 内部的资源包格式 Key 转换为实际 Minecraft 资源包的 Key
     *
     * 如 `(minecraft:)item/iron_sword_0` 转换为 `wakame:item/iron_sword_0`
     */
    private fun CreativeModel.toMinecraftFormat(): CreativeModel {
        val parent = parent()

        val newParent = if (parent != null && setModel(parent)) {
            // 如果父模型在 Wakame 内部资源包中生成成功, 则将其 Key 转换为 Minecraft 格式
            parent.namespace { RESOURCE_NAMESPACE }
        } else {
            // 如果父模型不在 Wakame 内部资源包中生成, 则保持不变
            parent()
        }

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
            .parent(newParent)
            .textures(
                ModelTextures.builder()
                    .layers(newLayers)
                    .particle(newParticle)
                    .variables(newVariables)
                    .build()
            )
            .build()
    }

    /**
     * 生成一个模型并添加到资源包中.
     *
     * @param originModelKey 原始配置文件中模型的 Key.
     * @return 是否成功生成模型.
     */
    private fun setModel(originModelKey: Key): Boolean {
        val modelFile = AssetUtils.getFile("models/${originModelKey.value()}", "json")
        if (modelFile == null) {
            // Skip vanilla models, they are already in the vanilla resource pack
            return false
        }
        val model = ModelSerializer.INSTANCE.deserialize(Readable.file(modelFile), originModelKey)
        val parent = model.parent()
        if (parent != null) {
            setModel(parent)
        }

        val newModel = model.toBuilder()
            .key(originModelKey.namespace { RESOURCE_NAMESPACE })
            .build()

        logger.info("Model for $originModelKey generated.")
        context.resourcePack.model(newModel)
        return true
    }
}

internal class ResourcePackMergePackGeneration(
    context: ResourcePackGenerationContext,
    private val packReader: ResourcePackReader<FileTreeReader>,
) : ResourcePackGeneration(context), KoinComponent {
    private val logger: Logger by inject()
    private val pluginDirectory: File by inject(named(PLUGIN_DATA_DIR))

    override fun process() {
        val serverPluginDirectory = pluginDirectory.parentFile
        val resourcePack = context.resourcePack
        val mergePacks = context.mergePacks
            .mapNotNull {
                val file = serverPluginDirectory.resolve(it)
                logger.info("Merging pack... path: $file")
                if (!file.exists()) {
                    logger.warn("Merge pack not found: $it")
                    return@mapNotNull null
                }
                file
            }
            .mapNotNull {
                if (it.isDirectory) {
                    packReader.readFromDirectory(it)
                } else {
                    if (it.extension != "zip") {
                        logger.warn("Invalid file extension for merge pack: ${it.extension}")
                        return@mapNotNull null
                    }
                    packReader.readFromZipFile(it)
                }
            }

        for (mergePack in mergePacks) {
            resourcePack.merge(mergePack, MergeStrategy.mergeAndKeepFirstOnError())
        }
    }
}

internal class ResourcePackModelSortGeneration(
    context: ResourcePackGenerationContext,
) : ResourcePackGeneration(context) {
    override fun process() {
        val resourcePack = context.resourcePack
        for (model in resourcePack.models()) {
            if (model.key().namespace() != Key.MINECRAFT_NAMESPACE) {
                continue
            }
            val newModelBuilder = model.toBuilder()
            val overrides = model.overrides()
            val sortedOverrides = overrides.sortedBy { it.customModelData() }
            newModelBuilder.overrides(sortedOverrides)
            resourcePack.model(newModelBuilder.build())
        }
    }

    private fun ItemOverride.customModelData(): Int? {
        val predicates = predicate()
        val customModelData = predicates.find { it.name() == "custom_model_data" }
        return customModelData?.value() as? Int
    }
}