package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.entity.EntityTypeHolder
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.krequire
import org.koin.core.qualifier.named
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

@Init(
    stage = InitStage.PRE_WORLD,
)
@Reload
object EntityRegistry {
    /**
     * The registry holding types of entities.
     */
    val TYPES: Registry<String, EntityTypeHolder> = SimpleRegistry()

    @InitFun
    fun init() {
        loadDataIntoRegistry()
    }

    @ReloadFun
    fun reload() {
        loadDataIntoRegistry()
    }

    private fun loadDataIntoRegistry() {
        val root = Injector.get<YamlConfigurationLoader>(named(ENTITY_GLOBAL_CONFIG_LOADER)).load()
        root.node("entity_type_holders").childrenMap().forEach { (_, n) ->
            val entityTypeHolder = n.krequire<EntityTypeHolder>()
            TYPES.register(entityTypeHolder.name, entityTypeHolder)
        }
    }
}