package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

@Init(
    stage = InitStage.PRE_WORLD
)
@Reload
object RarityRegistryConfigStorage : RegistryConfigStorage {
    const val FILE_PATH = "rarities.yml"

    @InitFun
    fun init() {
        KoishRegistries.RARITY.resetRegistry()
        applyDataToRegistry(KoishRegistries.RARITY::add)
        KoishRegistries.RARITY.freeze()
    }

    @ReloadFun
    fun reload() {
        applyDataToRegistry(KoishRegistries.RARITY::update)
    }

    private fun applyDataToRegistry(registryAction: (Identifier, RarityType) -> Unit) {
        val loader = buildYamlConfigLoader {
            withDefaults()
            serializers {
                register(GlowColorSerializer)
            }
        }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory(FILE_PATH).readText())
        for ((nodeKey, node) in rootNode.node("rarities").childrenMap()) {
            val entry = parseEntry(nodeKey, node)
            registryAction(entry.first, entry.second)
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
        val stringId = id.asMinimalString2()
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