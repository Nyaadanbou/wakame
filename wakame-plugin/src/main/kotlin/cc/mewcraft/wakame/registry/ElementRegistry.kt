package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.ElementProvider
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.krequire
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import xyz.xenondevs.commons.provider.Provider
import xyz.xenondevs.commons.provider.immutable.provider

@Init(
    stage = InitStage.PRE_WORLD
)
object ElementRegistry : KoinComponent, BiKnot<String, Element, Byte> {
    /**
     * The default element. By design, it should be the most common element.
     */
    val DEFAULT: Element by lazy { INSTANCES.values.first() }

    override val INSTANCES: Registry<String, Element> = SimpleRegistry()
    override val BI_LOOKUP: BiRegistry<String, Byte> = SimpleBiRegistry()

    private val providers: LoadingCache<String, Provider<Element>> = Caffeine
        .newBuilder()
        .weakValues()
        .build { k ->
            provider { INSTANCES.getOrNull(k) ?: DEFAULT }
        }

    fun getProvider(id: String): Provider<Element> {
        return providers[id]
    }

    @InitFun
    fun onPreWorld() {
        // 注册 ElementProvider
        ElementProvider.register(DefaultElementProvider)

        // 从配置文件加载元素
        loadConfiguration()
    }

//    override fun onReload() {
//        loadConfiguration()
//        updateProviders()
//    }

    private fun updateProviders() {
        providers.asMap().forEach { (_, provider) ->
            provider.update()
        }
    }

    private fun loadConfiguration() {
        INSTANCES.clear()
        BI_LOOKUP.clear()

        val root = get<NekoConfigurationLoader>(named(ELEMENT_GLOBAL_CONFIG_LOADER)).load()
        root.node("elements").childrenMap().forEach { (_, n) ->
            val element = n.krequire<Element>()
            // register element
            INSTANCES.register(element.uniqueId, element)
            // register element bi lookup
            BI_LOOKUP.register(element.uniqueId, element.binaryId)
        }
    }
}

private object DefaultElementProvider : ElementProvider {
    override fun get(id: String): Element? {
        return ElementRegistry.INSTANCES.getOrNull(id)
    }
}
