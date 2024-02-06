package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.reference.EntityReference
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

object EntityRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, EntityReference> by HashMapRegistry() {

    // configuration stuff
    private val loader: NekoConfigurationLoader by inject(named(ENTITY_CONFIG_LOADER))
    private lateinit var node: NekoConfigurationNode

    /**
     * Gets the key of specified [entity].
     *
     * @param entity the entity that you want lookup the key for
     * @return the key of the specified entity
     */
    fun getEntityKey(entity: Entity): Key {
        TODO("Not yet implemented")
    }

    private fun loadConfiguration() {
        node = loader.load()
        // TODO read config and populate values
    }

    override fun onPreWorld() {
        TODO("Not yet implemented")
    }

    override fun onReload() {
        TODO("Not yet implemented")
    }
}