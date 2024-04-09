package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.rarity.LevelMappings
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

/**
 * The registry of `level -> rarity` mappings.
 */
@PreWorldDependency(runBefore = [RarityRegistry::class])
@ReloadDependency(runBefore = [RarityRegistry::class])
object LevelMappingRegistry : KoinComponent, Initializable {
    const val GLOBAL_NAME = "global"
    const val CUSTOM_NAME = "custom"

    val INSTANCES: Registry<String, LevelMappings> = SimpleRegistry()

    override fun onPreWorld() = loadConfiguration()
    override fun onReload() = loadConfiguration()

    private fun loadConfiguration() {
        INSTANCES.clear()

        val root = get<NekoConfigurationLoader>(named(LEVEL_CONFIG_LOADER)).load()

        // deserialize the `global` mappings
        val globalLevelMappings = root.node(GLOBAL_NAME).krequire<LevelMappings>()
        // register the `global` mappings
        INSTANCES.register(GLOBAL_NAME, globalLevelMappings)

        // deserialize all custom mappings
        root.node(CUSTOM_NAME).childrenMap().forEach { (k, n) ->
            val rarityMappingsName = k.toString()
            val levelMappings = n.krequire<LevelMappings>()
            // register each custom mappings
            INSTANCES.register(rarityMappingsName, levelMappings)
        }
    }
}