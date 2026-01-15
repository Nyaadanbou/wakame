package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.api.element.ElementProvider
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.configurate.yamlLoader
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import cc.mewcraft.wakame.api.element.Element as ApiElement

@Init(InitStage.PRE_WORLD)
internal object ElementRegistryLoader : RegistryLoader {

    @InitFun
    fun init() {
        BuiltInRegistries.ELEMENT.resetRegistry()
        consumeData(BuiltInRegistries.ELEMENT::add)
        BuiltInRegistries.ELEMENT.freeze()

        ElementProvider.register(BuiltInElementProvider)
    }

    fun reload() {
        consumeData(BuiltInRegistries.ELEMENT::update)
    }

    private fun consumeData(registryAction: (Identifier, Element) -> Unit) {
        val rootDirectory = KoishDataPaths.CONFIGS.resolve("element/").toFile()

        // 获取元素的全局配置文件
        val globalConfigFile = rootDirectory.resolve("config.yml")

        // 获取元素的实例数据文件夹
        val entryDataDirectory = rootDirectory.resolve("entries/")

        val loader = yamlLoader {
            withDefaults()
        }

        // 加载所有元素实例, 并把它们添加进注册表
        entryDataDirectory.walk().drop(1).filter { it.isFile && it.extension == "yml" }.forEach { f ->
            try {
                val rootNode = loader.buildAndLoadString(f.readText())
                val entryId = f.relativeTo(entryDataDirectory).invariantSeparatorsPath.substringBeforeLast('.')
                val entryPair = parseEntry(entryId, rootNode)
                registryAction(entryPair.first, entryPair.second)
            } catch (e: Exception) {
                LOGGER.error("Failed to load element from file: ${f.toRelativeString(rootDirectory)}", e)
            }
        }
    }

    private fun parseEntry(nodeKey: Any, node: ConfigurationNode): Pair<Identifier, Element> {
        val id = Identifiers.of(nodeKey.toString())
        val displayName = node.node("name").get<Component>(Component.text(id.asString()))
        val displayStyles = node.node("styles").get<Array<StyleBuilderApplicable>>(emptyArray())

        return id to Element(displayName, displayStyles)
    }
}

private class ApiElementWrapper(private val element: Element) : ApiElement {
    override fun key(): Key {
        return element.key()
    }
}

private object BuiltInElementProvider : ElementProvider {
    override fun get(id: String): ApiElement? {
        return BuiltInRegistries.ELEMENT[id]?.let(::ApiElementWrapper)
    }
}