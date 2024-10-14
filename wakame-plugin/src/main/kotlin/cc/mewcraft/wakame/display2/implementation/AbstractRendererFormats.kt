package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.provider
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.yamlConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.*
import java.nio.file.Path
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.io.path.*
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/* 这里定义了可以在不同渲染器之间通用的 RendererFormats 实现 */

internal abstract class AbstractRendererFormats(
    protected val renderer: AbstractItemRenderer<*, *>,
) : RendererFormats, KoinComponent {
    protected val logger = get<Logger>()

    /**
     * 所有已加载的 [RendererFormat] 实例.
     */
    private val registeredRendererFormats = HashMap<String, RendererFormat>()

    /**
     * 所有已加载的 [Provider] 实例, 记录引用以支持热重载渲染器.
     */
    private val registeredFormatProviders = HashSet<Provider<out RendererFormat>>()

    /**
     * 配置文件 `id` -> 类型. 用于引导配置文件的序列化过程.
     */
    private val rendererFormatTypeById = HashMap<String, KType>()

    /**
     * 所有已加载的 [TextMetaFactory] 实例.
     */
    val textMetaFactoryRegistry = TextMetaFactoryRegistry()

    private fun cleanup() {
        registeredRendererFormats.clear()
        textMetaFactoryRegistry.reset()
    }

    /**
     * 初始化本实例的所有状态.
     */
    fun initialize(formatPath: Path) {
        cleanup()

        val loader = yamlConfig {
            withDefaults()
            serializers {
                registerAnnotatedObjects(
                    ObjectMapper.factoryBuilder()
                        .addNodeResolver(NodeResolver.nodeKey())
                        .addNodeResolver(NodeResolver.onlyWithSetting())
                        .addConstraint(Required::class.java, Constraint.required())
                        .addDiscoverer(dataClassFieldDiscoverer())
                        .build()
                )
            }
        }
        val root = loader.buildAndLoadString(formatPath.readText())

        for ((id, type) in rendererFormatTypeById) {
            val node = root.node(id)
            if (node.virtual()) {
                // TODO display2 当配置文件有缺省时, 支持回退到默认格式, 而不是直接抛异常
                logger.warn("Renderer format '$id' is not found in '${formatPath.relativeTo(get<Path>(named(PLUGIN_DATA_DIR)))}'. Fallback to default format.")
            }
            val format = node.krequire<RendererFormat>(type)

            // will overwrite the one already existing
            registeredRendererFormats[id] = format
            // create & register the text meta factory
            textMetaFactoryRegistry.registerFactory(format.createTextMetaFactory())

            logger.info("Loaded renderer format (${formatPath.parent.name}): $id")
        }

        // reload all renderer formats of this renderer
        registeredFormatProviders.forEach { provider -> provider.update() }
    }

    inline fun <reified F : RendererFormat> registerRendererFormat(id: String): Boolean {
        val previous = rendererFormatTypeById.put(id, typeOf<F>())
        if (previous != null) {
            logger.warn("Renderer format '$id' is already registered with type $previous")
            return false
        }
        return true
    }

    // 必须外面套个函数来访问 registeredFormats[id] 否则
    // lambda 好像会被自动 inline ??? 有时间再摸索一下
    @Suppress("UNCHECKED_CAST")
    private fun <F : RendererFormat> getRegisteredRendererFormat(id: String): F {
        return (registeredRendererFormats[id] as F?) ?: error("Renderer format '$id' is not registered")
    }

    fun <F : RendererFormat> getRendererFormatProvider(id: String): Provider<F> {
        val provider = provider { getRegisteredRendererFormat<F>(id) }
        registeredFormatProviders += provider
        return provider
    }

    @Suppress("UNCHECKED_CAST")
    override fun <F : RendererFormat> get(id: String): F? {
        return registeredRendererFormats[id] as F?
    }
}
