package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.rarity.RarityMappings
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.typedRequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

/**
 * The registry of `level -> rarity` mappings.
 */
object RarityMappingRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, RarityMappings> by HashMapRegistry() {

    // constants
    const val GLOBAL_RARITY_MAPPING_NAME: String = "global"

    // configuration stuff
    private val loader: NekoConfigurationLoader by inject(named(RARITY_CONFIG_LOADER))
    private lateinit var node: NekoConfigurationNode

    private fun loadConfiguration() {
        node = loader.load()

        // load the `global` mappings
        val globalRarityMappings = node.node("global_rarity_mappings").typedRequire<RarityMappings>()
        registerName2Object(GLOBAL_RARITY_MAPPING_NAME, globalRarityMappings)

        // load all custom mappings
        node.node("custom_rarity_mappings").childrenMap().forEach { (k, n) ->
            val rarityMappingsName = k.toString()
            val rarityMappings = n.typedRequire<RarityMappings>()
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