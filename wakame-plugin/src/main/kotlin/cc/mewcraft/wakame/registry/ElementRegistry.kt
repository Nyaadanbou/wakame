package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.typedRequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

object ElementRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, Element> by HashMapRegistry(),
    BiMapRegistry<String, Byte> by HashBiMapRegistry() {

    // default element
    val DEFAULT_ELEMENT: Element get() = name2ObjectMapping.values.first()

    // configuration stuff
    private val loader: NekoConfigurationLoader by inject(named(ELEMENT_CONFIG_LOADER))
    private lateinit var node: NekoConfigurationNode

    private fun loadConfiguration() {
        node = loader.load()
        with(node) {
            val elementsNode = node("elements")
            elementsNode.childrenMap().forEach { (_, n) ->
                val element = n.typedRequire<Element>()
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
