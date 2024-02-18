package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.requireKt
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

object ElementRegistry : KoinComponent, Initializable, Reloadable,
    Registry<String, Element> by HashMapRegistry(),
    BiMapRegistry<String, Byte> by HashBiMapRegistry() {

    /**
     * The default element. By design, it should be the most common element.
     */
    val DEFAULT_ELEMENT: Element by lazy { values.first() }

    // configuration stuff
    private lateinit var root: NekoConfigurationNode


    private fun loadConfiguration() {
        @OptIn(InternalApi::class) clearBoth()

        root = get<NekoConfigurationLoader>(named(ELEMENT_CONFIG_LOADER)).load()

        root.node("elements").childrenMap().forEach { (_, n) ->
            val element = n.requireKt<Element>()
            registerBothMapping(element.key, element)
        }
    }

    override fun onReload() {
        loadConfiguration()
    }

    override fun onPreWorld() {
        loadConfiguration()
    }
}
