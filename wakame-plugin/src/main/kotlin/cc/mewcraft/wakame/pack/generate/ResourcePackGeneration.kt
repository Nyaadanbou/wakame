@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.pack.AssetUtils
import cc.mewcraft.wakame.pack.ItemModelInfo
import cc.mewcraft.wakame.pack.RESOURCE_NAMESPACE
import cc.mewcraft.wakame.pack.VanillaResourcePack
import cc.mewcraft.wakame.util.adventure.withNamespace
import cc.mewcraft.wakame.util.adventure.withValue
import cc.mewcraft.wakame.util.readFromDirectory
import cc.mewcraft.wakame.util.readFromZipFile
import cc.mewcraft.wakame.util.text.mini
import net.kyori.adventure.key.Key
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.base.Writable
import team.unnamed.creative.metadata.pack.PackFormat
import team.unnamed.creative.metadata.pack.PackMeta
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures
import team.unnamed.creative.resources.MergeStrategy
import team.unnamed.creative.serialize.ResourcePackReader
import team.unnamed.creative.serialize.minecraft.fs.FileTreeReader
import team.unnamed.creative.serialize.minecraft.metadata.MetadataSerializer
import team.unnamed.creative.serialize.minecraft.model.ModelSerializer
import team.unnamed.creative.texture.Texture
import xyz.xenondevs.commons.collections.mapValuesNotNull
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
    abstract suspend fun process()
}

internal class ResourcePackMetaGeneration(
    context: ResourcePackGenerationContext,
) : ResourcePackGeneration(context) {
    override suspend fun process() {
        val packFormat = PackFormat.format(context.format, context.min, context.max)
        val packMeta = PackMeta.of(packFormat, context.description.mini)
        context.resourcePack.packMeta(packMeta)
    }
}

internal class ResourcePackIconGeneration(
    context: ResourcePackGenerationContext,
) : ResourcePackGeneration(context) {

    override suspend fun process() {
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

    override suspend fun process() {
        val resourcePack = context.resourcePack

        for (itemModelInfo in context.itemModelInfos) {
            LOGGER.info("Generating model for $itemModelInfo")
            try {
                val models = generateModel(itemModelInfo.modelKey())

                if (models.isEmpty()) {
                    resourcePack.setDefaultModel(itemModelInfo)
                    LOGGER.warn("No model to generate for $itemModelInfo, skip.")
                    continue
                }

                for (model in models) {
                    if (!resourcePack.setTextureBy(model)) {
                        resourcePack.setDefaultModel(itemModelInfo)
                        LOGGER.warn("Failed to generate texture for model $itemModelInfo, using default model.")
                        continue
                    }

                    LOGGER.info("Model for $itemModelInfo generated.")
                }
            } catch (e: Exception) {
                LOGGER.warn("Failed to generate model for $itemModelInfo. Reason: ${e.message}")
            }
        }
    }

    /**
     * 生存所有需要添加到资源包中的模型.
     *
     * @param modelKey 原始配置文件中模型的 Key.
     * @return 生成的模型列表. 如果模型不存在, 则返回空列表.
     */
    private fun generateModel(modelKey: Key): List<CreativeModel> {
        val models = arrayListOf<CreativeModel>()

        /**
         * 生成一个模型并添加到资源包中.
         *
         * @param originModelKey 原始配置文件中模型的 Key.
         * @return 是否成功生成模型.
         */
        fun addModel(originModelKey: Key): Boolean {
            val minecraftKey = originModelKey.withNamespace(RESOURCE_NAMESPACE) // 在真实资源包中的模型的 Key
            val modelFile = AssetUtils.getFile("models/${minecraftKey.value()}", "json")
            if (modelFile == null) {
                // Skip vanilla models, they are already in the vanilla resource pack
                return false
            }
            // Original model from config template
            val configModel = ModelSerializer.INSTANCE.deserialize(Readable.file(modelFile), minecraftKey)
            val parent = configModel.parent()
            if (parent != null) {
                addModel(parent)
            }

            for (override in configModel.overrides()) {
                val overrideModel = override.model()
                addModel(overrideModel)
            }

            models.add(configModel)
            return true
        }
        addModel(modelKey)

        return models
    }

    /**
     * 根据模型生成纹理并添加到资源包中.
     *
     * @param model 模型实例.
     * @return 是否成功生成纹理.
     */
    private fun ResourcePack.setTextureBy(model: CreativeModel): Boolean {
        // Set all textures from the model
        val textureLayers = model.textures().layers().map {
            setMinecraftTexture(it) { texture(it) }
        }
        val particle = model.textures().particle()?.let {
            setMinecraftTexture(it) { texture(it) }
        }
        val variables = model.textures().variables().mapValuesNotNull {
            setMinecraftTexture(it.value) { texture(it) }
        }

        // 将修改过的模型添加到资源包中
        val newModel = model.toBuilder()
            .textures(
                ModelTextures.builder()
                    .layers(textureLayers)
                    .particle(particle)
                    .variables(variables)
                    .build()
            )
            .build()

        model(newModel)

        return true
    }

    /**
     * 将原始纹理添加到资源包中.
     *
     * @param texture 原始纹理实例.
     * @param textureCallback 纹理实例回调函数, 用于应用纹理实例.
     * @return 更改后的模型纹理实例, 用于构建最终模型, 返回本身则表示不需要修改.
     */
    private fun setMinecraftTexture(texture: ModelTexture, textureCallback: (Texture) -> Unit): ModelTexture {
        val originTextureKey = texture.key() ?: return texture
        val textureFile = AssetUtils.getFile("textures/${originTextureKey.value()}", "png")
        if (textureFile == null) {
            return texture
        }
        val minecraftFormat = originTextureKey.withNamespace(RESOURCE_NAMESPACE)
        val textureWritable = Writable.file(textureFile)

        val texture = Texture.texture()
            .key(minecraftFormat.withValue { "$it.png" })
            .data(textureWritable)

        val metaFile = textureFile.resolveSibling("${textureFile.name}.mcmeta").takeIf { it.exists() }
        val meta = metaFile?.let { MetadataSerializer.INSTANCE.readFromTree(AssetUtils.toJsonElement(it)) }
        if (meta != null) {
            texture.meta(meta)
        }

        LOGGER.info("Texture for $originTextureKey generated.")
        textureCallback(texture.build())
        return ModelTexture.ofKey(minecraftFormat)
    }

    private suspend fun ResourcePack.setDefaultModel(itemModelInfo: ItemModelInfo) {
        val defaultModel = VanillaResourcePack.model(itemModelInfo.base)
            .map { it.toBuilder().key(itemModelInfo.modelKey()).build() }
            .onFailure {
                LOGGER.warn("Failed to get default model for $itemModelInfo. Reason: ${it.message}")
                return
            }
            .getOrThrow()

        model(defaultModel)
    }
}

internal class ResourcePackMergePackGeneration(
    context: ResourcePackGenerationContext,
    private val packReader: ResourcePackReader<FileTreeReader>,
) : ResourcePackGeneration(context) {

    override suspend fun process() {
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