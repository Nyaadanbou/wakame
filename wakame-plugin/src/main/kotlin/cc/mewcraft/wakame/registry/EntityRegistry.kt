package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.entity.EntityTypeHolder
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadableFun
import cc.mewcraft.wakame.reloader.ReloadableOrder
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

@Init(
    stage = InitStage.PRE_WORLD,
)
@Reload(
    order = ReloadableOrder.NORMAL
)
object EntityRegistry : KoinComponent {
    /**
     * The registry holding types of entities.
     */
    val TYPES: Registry<String, EntityTypeHolder> = SimpleRegistry()

    @InitFun
    fun onPreWorld() = loadConfiguration()
    @ReloadableFun
    fun onReload() = loadConfiguration()

    private fun loadConfiguration() {
        val root = get<NekoConfigurationLoader>(named(ENTITY_GLOBAL_CONFIG_LOADER)).load()
        root.node("entity_type_holders").childrenMap().forEach { (_, n) ->
            val entityTypeHolder = n.krequire<EntityTypeHolder>()
            TYPES.register(entityTypeHolder.name, entityTypeHolder)
        }
    }
}