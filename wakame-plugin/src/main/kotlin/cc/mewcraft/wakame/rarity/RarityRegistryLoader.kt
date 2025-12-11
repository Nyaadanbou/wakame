package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.serialization.configurate.serializer.NamedTextColorSerializer
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.yamlLoader
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.StyleBuilderApplicable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

@Init(
    stage = InitStage.PRE_WORLD
)
@Reload
internal object RarityRegistryLoader : RegistryLoader {

    @InitFun
    fun init() {
        BuiltInRegistries.RARITY.resetRegistry()
        consumeData(BuiltInRegistries.RARITY::add)
        BuiltInRegistries.RARITY.freeze()
    }

    @ReloadFun
    fun reload() {
        consumeData(BuiltInRegistries.RARITY::update)
    }

    private fun consumeData(registryAction: (Identifier, Rarity) -> Unit) {
        val rootDirectory = getFileInConfigDirectory("rarity/")

        // 获取稀有度的全局配置文件
        val globalConfigFile = rootDirectory.resolve("config.yml")

        // 获取稀有度的实例数据文件夹
        val entryDataDirectory = rootDirectory.resolve("entries/")

        val loader = yamlLoader {
            withDefaults()
            serializers {
                register(NamedTextColorSerializer)
            }
        }

        entryDataDirectory.walk().drop(1).filter { it.isFile && it.extension == "yml" }.forEach { f ->
            try {
                val rootNode = loader.buildAndLoadString(f.readText())
                val rarityId = f.relativeTo(entryDataDirectory).invariantSeparatorsPath.substringBeforeLast('.')
                val entry = parseEntry(rarityId, rootNode)
                registryAction(entry.first, entry.second)
            } catch (e: Exception) {
                LOGGER.error("Failed to load rarity from file: ${f.toRelativeString(rootDirectory)}", e)
            }
        }
    }

    /**
     * ## Node structure
     *
     * ```yaml
     * <root>:
     *   name: 史诗
     *   styles: []
     *   weight: 1
     *   color: red
     * ```
     */
    private fun parseEntry(nodeKey: Any, node: ConfigurationNode): Pair<Identifier, Rarity> {
        val id = Identifiers.of(nodeKey.toString())
        val name = node.node("name").get<Component>(Component.text(id.asString()))
        val styles = node.node("styles").get<Array<StyleBuilderApplicable>>(emptyArray())
        val weight = node.node("weight").get<Int>(0)
        val color = node.node("color").get<NamedTextColor>()
        val enchantSlotBase = node.node("enchant_slot_base").get<Int>(0)
        val enchantSlotLimit = node.node("enchant_slot_limit").get<Int>(Int.MIN_VALUE)
        val rarityType = Rarity(
            displayName = name,
            displayStyles = styles,
            weight = weight,
            color = color,
            enchantSlotBase = enchantSlotBase,
            enchantSlotLimit = enchantSlotLimit,
        )

        return id to rarityType
    }
}