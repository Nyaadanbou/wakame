package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.entity.attribute.AttributeBundleFacadeRegistryLoader
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryLoader
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.serialization.configurate.TypeSerializers
import cc.mewcraft.wakame.serialization.configurate.typeserializer.valueByNameTypeSerializer
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.register
import org.spongepowered.configurate.kotlin.extensions.get

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AttributeBundleFacadeRegistryLoader::class, // deps: 需要直接的数据, 必须在其之后
    ]
)
@Reload
internal object KizamiTypeRegistryLoader : RegistryLoader {

    @InitFun
    fun init() {
        KizamiEffectTypes // 初始化铭刻效果类型

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
        val rootDirectory = getFileInConfigDirectory("kizami/")

        // 获取铭刻的全局设置文件
        val globalConfigFile = rootDirectory.resolve("config.yml")

        // 获取铭刻的实例数据文件夹
        val entryDataDirectory = rootDirectory.resolve("entries/")

        val loader = buildYamlConfigLoader {
            withDefaults()
            serializers {
                register<KizamiType>(KizamiTypeSerializer)
                register<KizamiEffect>(TypeSerializers.dispatch(KizamiEffect::type, KizamiEffectType<*>::type))
                register(KizamiEffectType.REGISTRY.valueByNameTypeSerializer())
                register<KizamiEffectAttributeModifier>(KizamiEffectAttributeModifier.SERIALIZER)
            }
        }

        // 递归遍历文件夹 entryDataDirectory 里的每个文件
        // 文件名将作为铭刻的唯一 id (命名空间始终为默认值)
        entryDataDirectory.walk().drop(1).filter { it.isFile && it.extension == "yml" }.forEach { f ->
            try {
                val rootNode = loader.buildAndLoadString(f.readText())
                val kizamiId = Identifiers.of(f.toRelativeString(entryDataDirectory).substringBeforeLast('.'))

                // 开发日记 2024/8/31 小米
                // 因为铭刻现在是“一文件, 一铭刻” 所以文件内部无从得知铭刻的 id
                // 我们需要通过 hint 将铭刻 id (也就是文件名) 传递给序列化器
                rootNode.hint(RepresentationHints.KIZAMI_ID, kizamiId)

                val kizamiType = rootNode.get<KizamiType>() ?: error("Failed to parse kizami")
                registryAction(kizamiId, kizamiType)
            } catch (e: Exception) {
                LOGGER.error("Failed to load kizami from file: ${f.relativeTo(rootDirectory)}", e)
            }
        }
    }
}

private object BuiltInKizamiProvider : KizamiProvider {
    override fun get(id: String): Kizami? {
        return KoishRegistries.KIZAMI[id]
    }
}