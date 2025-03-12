package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.adventure.asMinimalStringKoish
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.require
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

@Init(stage = InitStage.PRE_WORLD)
@Reload
internal object ElementTypeRegistryLoader : RegistryConfigStorage {

    @InitFun
    fun init() {
        KoishRegistries.ELEMENT.resetRegistry()
        applyDataToRegistry(KoishRegistries.ELEMENT::add)
        KoishRegistries.ELEMENT.freeze()

        // register the provider
        ElementProvider.register(BuiltInElementProvider)
    }

    @ReloadFun
    fun reload() {
        // update existing registry
        applyDataToRegistry(KoishRegistries.ELEMENT::update)
    }

    private fun applyDataToRegistry(registryAction: (Identifier, ElementType) -> Unit) {
        val rootDirectory = KoishDataPaths.CONFIGS.resolve("element/").toFile()

        // 获取元素的全局配置文件
        val globalConfigFile = rootDirectory.resolve("config.yml")

        // 获取元素的实例数据文件夹
        val entryDataDirectory = rootDirectory.resolve("entries/")

        val loader = buildYamlConfigLoader {
            withDefaults()
        }

        // 加载所有元素实例, 并把它们添加进注册表
        entryDataDirectory.walk().drop(1).filter { it.isFile && it.extension == "yml" }.forEach { f ->
            try {
                val elementId = f.toRelativeString(entryDataDirectory).substringBeforeLast('.')
                val rootNode = loader.buildAndLoadString(f.readText())
                val entry = parseEntry(elementId, rootNode)
                registryAction(entry.first, entry.second)
            } catch (e: Exception) {
                LOGGER.error("Failed to load element from file: ${f.toRelativeString(rootDirectory)}", e)
            }
        }
    }

    private fun parseEntry(nodeKey: Any, node: ConfigurationNode): Pair<Identifier, ElementType> {
        val id = Identifiers.of(nodeKey.toString())
        val stringId = id.asMinimalStringKoish()
        val integerId = node.node("binary_index").require<Int>()
        val displayName = node.node("display_name").get<Component>(Component.text(stringId.replaceFirstChar(Char::titlecase)))
        val displayStyles = node.node("styles").get<Array<StyleBuilderApplicable>>(emptyArray())

        return id to ElementType(id, stringId, integerId, displayName, displayStyles)
    }
}

private object BuiltInElementProvider : ElementProvider {
    override fun get(stringId: String): ElementType? {
        return KoishRegistries.ELEMENT[stringId]
    }
}