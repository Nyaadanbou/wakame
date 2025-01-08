package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.rarity.LevelMappings
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

/**
 * The registry of `level -> rarity` mappings.
 */
@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [RarityRegistry::class]
)
@Reload(
    runAfter = [RarityRegistry::class]
)
object LevelMappingRegistry : KoinComponent {

    const val GLOBAL_NAME = "global"
    const val CUSTOM_NAME = "custom"

    val INSTANCES: Registry<String, LevelMappings> = SimpleRegistry()

    @InitFun
    fun init() = loadConfiguration()

    @ReloadFun
    fun reload() = loadConfiguration()

    private fun loadConfiguration() {
        INSTANCES.clear()

        val root = get<YamlConfigurationLoader>(named(LEVEL_GLOBAL_CONFIG_LOADER)).load()

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