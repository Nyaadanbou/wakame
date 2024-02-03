package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.rarity.RarityMappings
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.require
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named

/**
 * The registry of `level -> rarity` mappings.
 */
object RarityMappingRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, RarityMappings> by RegistryBase() {

    // constants
    const val GLOBAL_RARITY_MAPPING_NAME: String = "global"
    val CONFIG_LOADER_QUALIFIER: StringQualifier = named("rarity_mapping_config_loader")

    // configuration stuff
    private val configLoader: NekoConfigurationLoader by inject(CONFIG_LOADER_QUALIFIER)
    private lateinit var configNode: NekoConfigurationNode

    private fun loadConfiguration() {
        configNode = configLoader.load()

        // load the `global` mappings
        val globalRarityMappings = configNode.node("global_rarity_mappings").require<RarityMappings>()
        registerName2Object(GLOBAL_RARITY_MAPPING_NAME, globalRarityMappings)

        // load all custom mappings
        configNode.node("custom_rarity_mappings").childrenMap().forEach { (k, n) ->
            val rarityMappingsName = k.toString()
            val rarityMappings = n.require<RarityMappings>()
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