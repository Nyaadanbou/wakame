@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.pack.AssetUtils
import cc.mewcraft.wakame.pack.ItemModelInfo
import cc.mewcraft.wakame.pack.RESOURCE_NAMESPACE
import cc.mewcraft.wakame.pack.VanillaResourcePack
import cc.mewcraft.wakame.util.readFromDirectory
import cc.mewcraft.wakame.util.readFromZipFile
import cc.mewcraft.wakame.util.text.mini
import cc.mewcraft.wakame.util.withNamespace
import cc.mewcraft.wakame.util.withValue
import net.kyori.adventure.key.Key
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.base.Readable
import team.unnamed.creative.base.Writable
import team.unnamed.creative.metadata.pack.PackFormat
import team.unnamed.creative.metadata.pack.PackMeta
import team.unnamed.creative.model.ItemOverride
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures
import team.unnamed.creative.resources.MergeStrategy
import team.unnamed.creative.serialize.ResourcePackReader
import team.unnamed.creative.serialize.minecraft.fs.FileTreeReader
import team.unnamed.creative.serialize.minecraft.metadata.MetadataSerializer
import team.unnamed.creative.serialize.minecraft.model.ModelSerializer
import team.unnamed.creative.texture.Texture
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
) : ResourcePackGeneration(context) {

    override fun process() {
        val icon = KoishDataPaths.ASSETS.resolve("logo.png").toFile()
        if (!icon.exists()) {
            return
        }
        context.resourcePack.icon(Writable.file(icon))
    }
}

internal class ResourcePackCustomModelGeneration(
    context: ResourcePackGenerationContext,
) : ResourcePackGeneration(context) {

    override fun process() {
        val resourcePack = context.resourcePack

        for (itemModelInfo in context.itemModelInfos) {
            LOGGER.info("Generating model for $itemModelInfo")
            try {
                val models = generateModel(itemModelInfo.modelKey())

                if (models.isEmpty()) {
                    resourcePack.setDefaultModel(itemModelInfo)
                    LOGGER.warn("Failed to generate model for $itemModelInfo, using default model.")
                    continue
                }

                for (model in models) {
                    if (!resourcePack.setTexture(model)) {
                        resourcePack.setDefaultModel(itemModelInfo)
                        LOGGER.warn("Failed to generate texture for model $itemModelInfo, using default model.")
                        continue
                    }
                    resourcePack.model(model)

                    LOGGER.info("Model for $itemModelInfo generated.")
                }
            } catch (e: Exception) {
                LOGGER.warn("Failed to generate model for $itemModelInfo. Reason: ${e.message}")
            }
        }
    }

    private fun generateModel(modelKey: Key): List<CreativeModel> {
        val models = arrayListOf<CreativeModel>()

        /**
         * 生成一个模型并添加到资源包中.
         *
         * @param originModelKey 原始配置文件中模型的 Key.
         * @return 是否成功生成模型.
         */
        fun addModel(originModelKey: Key): Boolean {
            val modelFile = AssetUtils.getFile("models/${originModelKey.value()}", "json")
            if (modelFile == null) {
                // Skip vanilla models, they are already in the vanilla resource pack
                return false
            }
            // Original model from config template
            val configModel = ModelSerializer.INSTANCE.deserialize(Readable.file(modelFile), originModelKey)
            val parent = configModel.parent()
            val parentGenerated = if (parent != null) {
                addModel(parent)
            } else {
                false
            }

            for (override in configModel.overrides()) {
                val overrideModel = override.model()
                addModel(overrideModel)
            }

            models.add(configModel.toMinecraftFormat(parentGenerated))
            return true
        }
        addModel(modelKey)

        return models
    }

    /**
     * 生成一个纹理并添加到资源包中.
     *
     * @param model 模型实例.
     * @return 是否成功生成纹理.
     */
    private fun ResourcePack.setTexture(model: CreativeModel): Boolean {
        val texturesToGenerate = arrayListOf<ModelTexture>()

        // Set all textures from the model
        val textureLayers = model.textures().layers()
        val particle = model.textures().particle()
        val variables = model.textures().variables()

        textureLayers.forEach { layer -> texturesToGenerate.add(layer) }
        particle?.let { texturesToGenerate.add(it) }
        variables.forEach { (_, value) -> texturesToGenerate.add(value) }

        if (texturesToGenerate.isEmpty()) {
            return false
        }

        for (texture in texturesToGenerate) {
            val originTextureKey = texture.key() ?: continue
            val textureFile = AssetUtils.getFile("textures/${originTextureKey.value()}", "png")
            if (textureFile == null) {
                return false
            }
            val textureWritable = Writable.file(textureFile)

            val texture = Texture.texture()
                .key(originTextureKey.withValue { "$it.png" })
                .data(textureWritable)

            val metaFile = textureFile.resolveSibling("${textureFile.name}.mcmeta").takeIf { it.exists() }
            val meta = metaFile?.let { MetadataSerializer.INSTANCE.readFromTree(AssetUtils.toJsonElement(it)) }
            if (meta != null) {
                texture.meta(meta)
            }

            LOGGER.info("Texture for $originTextureKey generated.")
            texture(texture.build())
        }

        return true
    }

    private fun ResourcePack.setDefaultModel(itemModelInfo: ItemModelInfo) {
        val defaultModel = VanillaResourcePack.model(itemModelInfo.base)
            .map { it.toBuilder().key(itemModelInfo.modelKey()).build() }
            .onFailure {
                LOGGER.warn("Failed to get default model for $itemModelInfo. Reason: ${it.message}")
                return
            }
            .getOrThrow()

        model(defaultModel)
    }

    /**
     * 将 Wakame 内部的资源包格式 Key 转换为实际 Minecraft 资源包的 Key
     *
     * 如 `(minecraft:)item/iron_sword_0` 转换为 `wakame:item/iron_sword_0`
     */
    private fun CreativeModel.toMinecraftFormat(parentGenerated: Boolean): CreativeModel {
        val parent = parent()
        val newParent = if (parentGenerated) {
            parent?.withNamespace(RESOURCE_NAMESPACE)
        } else {
            parent
        }

        val modelTextures = textures()
        val newLayers = modelTextures.layers().map { texture ->
            val oldKey = texture.key()
            val newKey = oldKey!!.withNamespace(RESOURCE_NAMESPACE)
            ModelTexture.ofKey(newKey)
        }

        val newParticle = modelTextures.particle()?.let { texture ->
            val oldKey = texture.key()
            val newKey = oldKey!!.withNamespace(RESOURCE_NAMESPACE)
            ModelTexture.ofKey(newKey)
        }

        val newVariables = modelTextures.variables().map { (name, value) ->
            val oldKey = value.key()
            val newKey = oldKey!!.withNamespace(RESOURCE_NAMESPACE)
            name to ModelTexture.ofKey(newKey)
        }.toMap()

        val itemOverrides = overrides()
        val newOverrides = itemOverrides.map { override ->
            val oldKey = override.model()
            val newKey = oldKey.withNamespace(RESOURCE_NAMESPACE)
            ItemOverride.of(newKey, override.predicate())
        }

        return toBuilder()
            .key(key().withNamespace(RESOURCE_NAMESPACE))
            .parent(newParent)
            .textures(
                ModelTextures.builder()
                    .layers(newLayers)
                    .particle(newParticle)
                    .variables(newVariables)
                    .build()
            )
            .overrides(newOverrides)
            .build()
    }
}

internal class ResourcePackMergePackGeneration(
    context: ResourcePackGenerationContext,
    private val packReader: ResourcePackReader<FileTreeReader>,
) : ResourcePackGeneration(context) {

    override fun process() {
        // TODO 允许测试环境正常运行
        val serverPluginDirectory = SERVER.pluginsFolder
        val resourcePack = context.resourcePack
        val mergePacks = context.mergePacks
            .mapNotNull {
                val file = serverPluginDirectory.resolve(it)
                LOGGER.info("Merging pack... path: $file")
                if (!file.exists()) {
                    LOGGER.warn("Merge pack not found: $it")
                    return@mapNotNull null
                }
                file
            }
            .mapNotNull {
                if (it.isDirectory) {
                    packReader.readFromDirectory(it)
                } else {
                    if (it.extension != "zip") {
                        LOGGER.warn("Invalid file extension for merge pack: ${it.extension}")
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