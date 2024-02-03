package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.require
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named

object ElementRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, Element> by RegistryBase(),
    BiMapRegistry<String, Byte> by BiMapRegistryBase() {

    // default element
    val DEFAULT_ELEMENT: Element get() = name2ObjectMapping.values.first()

    // constants
    val CONFIG_LOADER_QUALIFIER: StringQualifier = named("element_config_loader")

    // configuration stuff
    private val configLoader: NekoConfigurationLoader by inject(CONFIG_LOADER_QUALIFIER)
    private lateinit var configNode: NekoConfigurationNode

    private fun loadConfiguration() {
        configNode = configLoader.load()
        with(configNode) {
            val elementsNode = node("elements")
            elementsNode.childrenMap().forEach { (_, n) ->
                val element = n.require<Element>()
                registerBothMapping(element.name, element)
            }
        }
    }

    override fun onReload() {
        loadConfiguration()
    }

    override fun onPreWorld() {
        loadConfiguration()
    }
}
