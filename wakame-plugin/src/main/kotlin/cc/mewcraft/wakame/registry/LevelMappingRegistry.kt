package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.initializer.DependencyConfig
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.rarity.LevelMappings
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.requireKt
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

/**
 * The registry of `level -> rarity` mappings.
 */
@DependencyConfig(
    preWorldBefore = [RarityRegistry::class]
)
object LevelMappingRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, LevelMappings> by HashMapRegistry() {

    // constants
    const val GLOBAL_NAME: String = "global"

    // configuration stuff
    private lateinit var root: NekoConfigurationNode

    private fun loadConfiguration() {
        @OptIn(InternalApi::class) clearName2Object()

        root = get<NekoConfigurationLoader>(named(LEVEL_CONFIG_LOADER)).load()

        // deserialize the `global` mappings
        val globalLevelMappings = root.node("global").requireKt<LevelMappings>()
        registerName2Object(GLOBAL_NAME, globalLevelMappings)

        // deserialize all custom mappings
        root.node("custom").childrenMap().forEach { (k, n) ->
            val rarityMappingsName = k.toString()
            val levelMappings = n.requireKt<LevelMappings>()
            registerName2Object(rarityMappingsName, levelMappings)
        }
    }

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }
}