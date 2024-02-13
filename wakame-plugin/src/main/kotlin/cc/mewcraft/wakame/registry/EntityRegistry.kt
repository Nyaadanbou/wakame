package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.reference.EntityReference
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.requireKt
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

object EntityRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, EntityReference> by HashMapRegistry() {

    // configuration stuff
    private lateinit var root: NekoConfigurationNode

    @OptIn(InternalApi::class)
    private fun loadConfiguration() {
        clearName2Object()

        root = get<NekoConfigurationLoader>(named(ENTITY_CONFIG_LOADER)).load()

        root.node("entity_references").childrenMap().forEach { (_, n) ->
            val entityReference = n.requireKt<EntityReference>()
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