package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.entity.attribute.AttributeFacadeRegistryLoader
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.serialization.configurate.RepresentationHints
import cc.mewcraft.wakame.serialization.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.yamlLoader
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AttributeFacadeRegistryLoader::class, // deps: 需要直接的数据, 必须在其之后
    ]
)
internal object KizamiRegistryLoader : RegistryLoader {

    @InitFun
    fun init() {
        BuiltInRegistries.KIZAMI.resetRegistry()
        consumeData(BuiltInRegistries.KIZAMI::add)
        BuiltInRegistries.KIZAMI.freeze()
    }

    fun reload() {
        consumeData(BuiltInRegistries.KIZAMI::update)
    }

    private fun consumeData(registryAction: (Identifier, Kizami) -> Unit) {
        val rootDirectory = getFileInConfigDirectory("kizami/")

        // 获取铭刻的全局设置文件
        val globalConfigFile = rootDirectory.resolve("config.yml")

        // 获取铭刻的实例数据文件夹
        val entryDataDirectory = rootDirectory.resolve("entries/")

        val loader = yamlLoader {
            withDefaults()
            serializers {
                register<KizamiEffectAttributeModifier>(KizamiEffectAttributeModifier.SERIALIZER)
                register(
                    DispatchingSerializer.createPartial<String, KizamiEffect>(
                        mapOf(
                            "attribute_modifier" to KizamiEffectAttributeModifier::class,
                            // TODO 支持技能
                        )
                    )
                )
            }
        }

        // 递归遍历文件夹 entryDataDirectory 里的每个文件
        // 文件名将作为铭刻的唯一 id (命名空间始终为默认值)
        entryDataDirectory.walk().drop(1).filter { it.isFile && it.extension == "yml" }.forEach { f ->
            try {
                val rootNode = loader.buildAndLoadString(f.readText())
                val entryId = Identifiers.of(f.relativeTo(entryDataDirectory).invariantSeparatorsPath.substringBeforeLast('.'))
                val entryVal = parseEntry(entryId, rootNode)
                registryAction(entryId, entryVal)
            } catch (e: Exception) {
                LOGGER.error("Failed to load kizami from file: ${f.relativeTo(rootDirectory)}", e)
            }
        }
    }

    /**
     * The serializer of kizami type.
     *
     * ## Node structure
     *
     * ```yaml
     * <node>:
     *   name: <string>
     *   styles: <string>
     *   effects:
     *     0:
     *       - type: <effect key>
     *         k1: v1
     *         k2: v2
     *         ...
     *       - type: <effect key>
     *         k1: v1
     *         k2: v2
     *     1:
     *       - type: <effect key>
     *         k1: v1
     *         k2: v2
     *         ...
     *       - type: <effect key>
     *         k1: v1
     *         k2: v2
     * ```
     */
    private fun parseEntry(id: Identifier, node: ConfigurationNode): Kizami {
        val name = node.node("name").get<Component>(Component.text(id.asString()))
        val styles = node.node("styles").get<Array<StyleBuilderApplicable>>(emptyArray())

        // 某些 KizamiEffect 的序列化需要知道当前铭刻的 id.
        // 这里通过 RepresentationHint 将 id 传递给序列化.
        node.hint(RepresentationHints.KIZAMI_ID, id)
        val effects = node.node("effects").get<Map<Int, List<KizamiEffect>>>(emptyMap())
        require(effects.keys.all { it >= 0 }) { "All the keys of kizami effects must be positive integers" }

        return Kizami(
            displayName = name,
            displayStyles = styles,
            effects = effects
        )
    }
}
