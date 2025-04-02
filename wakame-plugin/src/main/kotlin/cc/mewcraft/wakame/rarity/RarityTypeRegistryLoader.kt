package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryLoader
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.adventure.asMinimalStringKoish
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.yamlLoader
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

@Init(
    stage = InitStage.PRE_WORLD
)
@Reload
internal object RarityTypeRegistryLoader : RegistryLoader {

    @InitFun
    fun init() {
        KoishRegistries.RARITY.resetRegistry()
        applyDataToRegistry(KoishRegistries.RARITY::add)
        KoishRegistries.RARITY.freeze()
    }

    @InitFun
    fun reload() {
        applyDataToRegistry(KoishRegistries.RARITY::update)
    }

    private fun applyDataToRegistry(registryAction: (Identifier, RarityType) -> Unit) {
        val rootDirectory = getFileInConfigDirectory("rarity/")

        // 获取稀有度的全局配置文件
        val globalConfigFile = rootDirectory.resolve("config.yml")

        // 获取稀有度的实例数据文件夹
        val entryDataDirectory = rootDirectory.resolve("entries/")

        val loader = yamlLoader {
            withDefaults()
            serializers {
                register(GlowColorSerializer)
            }
        }

        entryDataDirectory.walk().drop(1).filter { it.isFile && it.extension == "yml" }.forEach { f ->
            try {
                val rootNode = loader.buildAndLoadString(f.readText())
                val rarityId = f.toRelativeString(entryDataDirectory).substringBeforeLast('.')
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
     * epic:
     *   binary_index: 3
     *   display_name: 史诗
     *   styles: []
     *   weight: 1
     *   glow_color: red
     *   ...
     * ```
     */
    private fun parseEntry(nodeKey: Any, node: ConfigurationNode): Pair<Identifier, RarityType> {
        val id = Identifiers.of(nodeKey.toString())
        val stringId = id.asMinimalStringKoish()
        val integerId = node.node("binary_index").require<Int>()
        val displayName = node.node("display_name").require<Component>()
        val displayStyles = node.node("styles").require<Array<StyleBuilderApplicable>>()
        val weight = node.node("weight").get<Int>(0)
        val glowColor = node.node("glow_color").require<GlowColor>()
        val rarityType = RarityType(
            id,
            stringId,
            integerId,
            displayName,
            displayStyles,
            weight,
            glowColor
        )

        return id to rarityType
    }
}