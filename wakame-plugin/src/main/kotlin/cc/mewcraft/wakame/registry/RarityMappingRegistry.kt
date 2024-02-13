package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.rarity.RarityMappings
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.requireKt
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

/**
 * The registry of `level -> rarity` mappings.
 */
object RarityMappingRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, RarityMappings> by HashMapRegistry() {

    // constants
    const val GLOBAL_RARITY_MAPPING_NAME: String = "global"

    // configuration stuff
    private lateinit var node: NekoConfigurationNode

    @OptIn(InternalApi::class)
    private fun loadConfiguration() {
        clearName2Object()

        node = get<NekoConfigurationLoader>(named(RARITY_MAPPING_CONFIG_LOADER)).load()

        // deserialize the `global` mappings
        val globalRarityMappings = node.node("global_rarity_mappings").requireKt<RarityMappings>()
        registerName2Object(GLOBAL_RARITY_MAPPING_NAME, globalRarityMappings)

        // deserialize all custom mappings
        node.node("custom_rarity_mappings").childrenMap().forEach { (k, n) ->
            val rarityMappingsName = k.toString()
            val rarityMappings = n.requireKt<RarityMappings>()
            registerName2Object(rarityMappingsName, rarityMappings)
        }
    }

    override fun onReload() {
        loadConfiguration()
    }

    override fun onPreWorld() {
        loadConfiguration()
    }
}