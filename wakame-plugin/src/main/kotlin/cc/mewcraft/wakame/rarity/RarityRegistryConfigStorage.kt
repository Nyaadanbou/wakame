package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.asMinimalString2
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
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
                kregister(GlowColorSerializer)
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
        val integerId = node.node("binary_index").krequire<Int>()
        val displayName = node.node("display_name").krequire<Component>()
        val displayStyles = node.node("styles").krequire<Array<StyleBuilderApplicable>>()
        val weight = node.node("weight").get<Int>(0)
        val glowColor = node.node("glow_color").krequire<GlowColor>()
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