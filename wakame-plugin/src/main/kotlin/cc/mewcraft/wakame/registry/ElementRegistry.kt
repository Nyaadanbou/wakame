package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

object ElementRegistry : KoinComponent, Initializable, BiKnot<String, Element, Byte> {
    /**
     * The default element. By design, it should be the most common element.
     */
    val DEFAULT: Element by lazy(
        LazyThreadSafetyMode.NONE // harmless race condition
    ) { INSTANCES.objects.first() }

    override val INSTANCES: Registry<String, Element> = SimpleRegistry()
    override val BI_LOOKUP: BiRegistry<String, Byte> = SimpleBiRegistry()

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }

    private fun loadConfiguration() {
        INSTANCES.clear()
        BI_LOOKUP.clear()

        val root = get<NekoConfigurationLoader>(named(ELEMENT_CONFIG_LOADER)).load()
        root.node("elements").childrenMap().forEach { (_, n) ->
            val element = n.krequire<Element>()
            // register element
            INSTANCES.register(element.uniqueId, element)
            // register element bi lookup
            BI_LOOKUP.register(element.uniqueId, element.binaryId)
        }
    }
}
