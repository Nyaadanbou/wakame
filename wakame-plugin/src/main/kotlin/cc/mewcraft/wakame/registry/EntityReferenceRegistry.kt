package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.reference.EntityReference
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

object EntityReferenceRegistry : KoinComponent, Initializable {
    val INSTANCES: Registry<String, EntityReference> = SimpleRegistry()

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }

    private fun loadConfiguration() {
        val root = get<NekoConfigurationLoader>(named(ENTITY_CONFIG_LOADER)).load()
        root.node("entity_references").childrenMap().forEach { (_, n) ->
            val entityReference = n.krequire<EntityReference>()
            INSTANCES.register(entityReference.name, entityReference)
        }
    }
}