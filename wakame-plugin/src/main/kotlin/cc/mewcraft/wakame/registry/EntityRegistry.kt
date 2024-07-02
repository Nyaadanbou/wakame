package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.entity.EntityTypeHolder
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

object EntityRegistry : KoinComponent, Initializable {
    /**
     * The registry holding types of entities.
     */
    val TYPES: Registry<String, EntityTypeHolder> = SimpleRegistry()

    override fun onPreWorld() = loadConfiguration()
    override fun onReload() = loadConfiguration()

    private fun loadConfiguration() {
        val root = get<NekoConfigurationLoader>(named(ENTITY_GLOBAL_CONFIG_LOADER)).load()
        root.node("entity_type_holders").childrenMap().forEach { (_, n) ->
            val entityTypeHolder = n.krequire<EntityTypeHolder>()
            TYPES.register(entityTypeHolder.name, entityTypeHolder)
        }
    }
}