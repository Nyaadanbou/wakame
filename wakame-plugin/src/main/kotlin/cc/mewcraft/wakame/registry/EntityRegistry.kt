package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.reference.EntityReference
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.typedRequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

object EntityRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, EntityReference> by HashMapRegistry() {

    // configuration stuff
    private val loader: NekoConfigurationLoader by inject(named(ENTITY_CONFIG_LOADER))
    private lateinit var node: NekoConfigurationNode

    @OptIn(InternalApi::class)
    private fun loadConfiguration() {
        clearName2Object()

        node = loader.load()
        node.node("entity_references").childrenMap().forEach { (_, n) ->
            val entityReference = n.typedRequire<EntityReference>()
            registerName2Object(entityReference.name, entityReference)
        }
    }

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }
}