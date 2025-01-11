package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.core.Identifier
import cc.mewcraft.wakame.core.Identifiers
import cc.mewcraft.wakame.core.RegistryConfigStorage
import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.entity.attribute.AttributeBundleFacadeRegistryConfigStorage
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.serialization.configurate.typeserializer.Serializers
import cc.mewcraft.wakame.serialization.configurate.typeserializer.valueByNameTypeSerializer
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.kregister
import org.spongepowered.configurate.kotlin.extensions.get
import java.io.File

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [AttributeBundleFacadeRegistryConfigStorage::class]
)
@Reload(
    runAfter = []
)
internal object KizamiRegistryConfigStorage : RegistryConfigStorage {

    /**
     * 存放铭刻的文件夹 (相对于插件文件夹).
     */
    const val DIR_PATH: String = "kizamiz/"

    @InitFun
    fun init() {
        KoishRegistries.KIZAMI.resetRegistry()
        applyDataToRegistry(KoishRegistries.KIZAMI::add)
        KoishRegistries.KIZAMI.freeze()

        KizamiProvider.register(BuiltInKizamiProvider)
    }

    @ReloadFun
    fun reload() {
        applyDataToRegistry(KoishRegistries.KIZAMI::update)
    }

    private fun applyDataToRegistry(registryAction: (Identifier, KizamiType) -> Unit) {
        KizamiEffectTypes // 初始化铭刻效果类型

        val loader = buildYamlConfigLoader {
            withDefaults()
            serializers {
                kregister(KizamiTypeSerializer)
                kregister(Serializers.dispatching(KizamiEffect::type, KizamiEffectType<*>::type))
                kregister(KizamiEffectType.REGISTRY.valueByNameTypeSerializer())
                kregister(KizamiEffectAttributeModifier.SERIALIZER)
                kregister(KizamiEffectPlayerAbility.SERIALIZER)
            }
        }

        // 递归遍历文件夹 DIR_PATH 里的每个文件
        // 文件名将作为铭刻的 id (命名空间始终默认)
        val dir = getFileInConfigDirectory(DIR_PATH)
        for (file in dir.walk().drop(1).filter(File::isFile)) {
            try {
                val rootNode = loader.buildAndLoadString(file.readText())
                val kizamiId = Identifiers.ofKoish(file.nameWithoutExtension)

                // 开发日记 2024/8/31 小米
                // 因为铭刻现在是“一文件, 一铭刻” 所以文件内部无从得知铭刻的 id
                // 我们需要通过 hint 将铭刻 id (也就是文件名) 传递给序列化器
                rootNode.hint(RepresentationHints.KIZAMI_ID, kizamiId)

                val kizamiType = rootNode.get<KizamiType>() ?: error("Failed to parse kizami")
                registryAction(kizamiId, kizamiType)
            } catch (e: Exception) {
                LOGGER.error("Failed to load kizami from file: ${file.relativeTo(dir)}", e)
            }
        }
    }
}

private object BuiltInKizamiProvider : KizamiProvider {
    override fun get(id: String): Kizami? {
        return KoishRegistries.KIZAMI[id]
    }
}